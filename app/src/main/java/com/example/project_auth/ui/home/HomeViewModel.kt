package com.example.project_auth.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.FirestoreRepository
import com.example.project_auth.data.QuoteCache
import com.example.project_auth.data.QuoteLoader
import com.example.project_auth.data.StockRepository
import com.example.project_auth.data.toSnapshot
import com.example.project_auth.data.remote.Quote
import com.example.project_auth.domain.Companies
import com.example.project_auth.domain.Company
import com.example.project_auth.domain.Sector
import com.example.project_auth.domain.nextQuoteBatch
import com.example.project_auth.domain.planQuoteLoad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** [quote] is null when Finnhub could not provide it. */
data class CompanyQuote(val company: Company, val quote: Quote?)

data class HomeUiState(
    val sector: Sector = Sector.CLOUD,
    val items: List<CompanyQuote> = emptyList(),
    val loading: Boolean = false,
    val loaded: Boolean = false,
    /** Device time (epoch millis) of the oldest saved quote being shown instead of a fresh one. */
    val offlineSince: Long? = null,
    val sort: SortOrder = SortOrder(),
) {
    val failed: Boolean get() = loaded && items.none { it.quote != null }
}

class HomeViewModel(
    private val repository: StockRepository = StockRepository(),
    private val firestore: FirestoreRepository = FirestoreRepository(),
    cache: QuoteCache = QuoteCache.instance,
    private val loader: QuoteLoader = QuoteLoader(repository::getQuotes, cache::load, cache::save),
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    /** Stale symbols of the current sector whose request waits until their row is needed. */
    private var waiting: Set<String> = emptySet()

    /** Index, in the order the rows are shown, of the last row on screen. */
    private var lastVisible = -1

    init {
        load()
    }

    fun selectSector(sector: Sector) {
        if (sector == _state.value.sector) return
        waiting = emptySet()
        lastVisible = -1
        _state.update {
            it.copy(sector = sector, items = emptyList(), loading = false, loaded = false, offlineSince = null)
        }
        load()
    }

    fun selectSort(field: SortField) {
        _state.update { it.copy(sort = it.sort.select(field)) }
        // Ordering by price compares every company, so it cannot wait for the rows to be seen.
        if (_state.value.sort.field == SortField.PRICE) fetchWaiting(waiting.toList())
    }

    /** Ignored while a load is running, so a second pull cannot request the same quotes again. */
    fun refresh() {
        if (_state.value.loading) return
        load(force = true)
    }

    /**
     * Shows the quotes already known (saved on the device or fetched earlier) straight away, then
     * asks for the stale ones: only the first rows, plus any row with no quote at all, and the rest
     * wait in [waiting]. A quote fetched less than a minute ago is not asked for again unless
     * [force] is set (pull-to-refresh). Any symbol Finnhub cannot answer keeps its old quote, and
     * [HomeUiState.offlineSince] tells the UI that some of what it shows is old.
     */
    private fun load(force: Boolean = false) {
        val sector = _state.value.sector
        val companies = Companies.bySector(sector)
        val symbols = companies.map { it.symbol }
        loader.restore(symbols)

        val plan = planQuoteLoad(
            displayOrder = displayOrder(companies),
            stale = loader.staleOf(symbols, force),
            hasQuote = loader::hasQuote,
            all = _state.value.sort.field == SortField.PRICE,
        )
        waiting = plan.later.toSet()
        if (plan.now.isEmpty()) {
            publish(sector, loading = false)
            return
        }
        _state.update { state ->
            state.copy(
                loading = true,
                items = if (symbols.any(loader::hasQuote)) itemsOf(companies) else state.items,
            )
        }
        viewModelScope.launch {
            saveSnapshots(loader.fetch(plan.now))
            publish(sector, loading = false)
            if (_state.value.sector == sector) fetchVisibleWaiting()
        }
    }

    /**
     * Called as the list scrolls. Once the rows being shown get close to the ones that wait, the
     * next batch is requested, so they arrive already updated. Ignored while a load is running;
     * it is checked again when that load ends.
     */
    fun onRowsVisible(lastVisibleIndex: Int) {
        lastVisible = lastVisibleIndex
        fetchVisibleWaiting()
    }

    private fun fetchVisibleWaiting() {
        val state = _state.value
        if (state.loading || waiting.isEmpty()) return
        val order = displayOrder(Companies.bySector(state.sector))
        fetchWaiting(nextQuoteBatch(order, waiting, lastVisible))
    }

    private fun fetchWaiting(batch: List<String>) {
        if (batch.isEmpty()) return
        waiting -= batch.toSet()
        val sector = _state.value.sector
        viewModelScope.launch {
            saveSnapshots(loader.fetch(batch))
            publish(sector)
        }
    }

    /** Ignored if the user already moved to another sector: its quotes were still saved by the loader. */
    private fun publish(sector: Sector, loading: Boolean? = null) {
        _state.update { state ->
            if (state.sector != sector) return@update state
            val companies = Companies.bySector(sector)
            state.copy(
                items = itemsOf(companies),
                loading = loading ?: state.loading,
                loaded = true,
                offlineSince = loader.offlineSince(companies.map { it.symbol }),
            )
        }
    }

    private fun itemsOf(companies: List<Company>) = companies.map { CompanyQuote(it, loader.quoteOf(it.symbol)) }

    private fun displayOrder(companies: List<Company>): List<String> =
        itemsOf(companies).sortedWith(_state.value.sort).map { it.company.symbol }

    /** Runs on its own scope so a slow or failing write never blocks the quote list. */
    private fun saveSnapshots(quotes: Map<String, Quote>) {
        val snapshots = quotes.map { (symbol, quote) -> quote.toSnapshot(symbol) }
        viewModelScope.launch { firestore.saveSnapshots(snapshots) }
    }
}
