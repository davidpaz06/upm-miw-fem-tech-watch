package com.example.project_auth.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.FirestoreRepository
import com.example.project_auth.data.QuoteCache
import com.example.project_auth.data.QuoteLoader
import com.example.project_auth.data.StockRepository
import com.example.project_auth.domain.Companies
import com.example.project_auth.domain.nextQuoteBatch
import com.example.project_auth.domain.planQuoteLoad
import com.example.project_auth.ui.home.CompanyQuote
import com.example.project_auth.ui.home.SortField
import com.example.project_auth.ui.home.SortOrder
import com.example.project_auth.ui.home.sortedWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val items: List<CompanyQuote> = emptyList(),
    val loading: Boolean = true,
    /** Device time (epoch millis) of the oldest saved quote shown instead of a fresh one. */
    val offlineSince: Long? = null,
    val sort: SortOrder = SortOrder(),
)

class WatchlistViewModel(
    repository: StockRepository = StockRepository(),
    private val firestore: FirestoreRepository = FirestoreRepository(),
    cache: QuoteCache = QuoteCache.instance,
    private val loader: QuoteLoader = QuoteLoader(repository::getQuotes, cache::load, cache::save),
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistUiState())
    val state: StateFlow<WatchlistUiState> = _state.asStateFlow()

    private var symbols: Set<String> = emptySet()

    /** Stale favourites whose request waits until their row is needed. */
    private var waiting: Set<String> = emptySet()

    /** Index, in the order the rows are shown, of the last row on screen. */
    private var lastVisible = -1

    init {
        viewModelScope.launch {
            firestore.watchlistSymbols()
                .catch { emit(emptySet()) }
                .collectLatest { sync(it, force = false) }
        }
    }

    /** Ignored while a load is running, so a second pull cannot request the same quotes again. */
    fun refresh() {
        if (_state.value.loading) return
        viewModelScope.launch { sync(symbols, force = true) }
    }

    fun selectSort(field: SortField) {
        _state.value = _state.value.let { it.copy(sort = it.sort.select(field)) }
        // Ordering by price compares every company, so it cannot wait for the rows to be seen.
        if (_state.value.sort.field == SortField.PRICE) fetchWaiting(waiting.toList())
    }

    /**
     * Called as the list scrolls. Once the rows being shown get close to the ones that wait, the
     * next batch is requested. Ignored while a load is running; it is checked again when it ends.
     */
    fun onRowsVisible(lastVisibleIndex: Int) {
        lastVisible = lastVisibleIndex
        fetchVisibleWaiting()
    }

    /**
     * Shows the quotes already known (saved on the device or fetched earlier) straight away, then
     * asks for the stale ones: the first rows plus any row with no quote at all, and the rest
     * wait in [waiting]. A quote fetched less than a minute ago is not asked for again unless
     * [force] is set (pull-to-refresh).
     */
    private suspend fun sync(newSymbols: Set<String>, force: Boolean) {
        symbols = newSymbols
        loader.restore(newSymbols)
        val plan = planQuoteLoad(
            displayOrder = displayOrder(),
            stale = loader.staleOf(newSymbols, force),
            hasQuote = loader::hasQuote,
            all = _state.value.sort.field == SortField.PRICE,
        )
        waiting = plan.later.toSet()
        publish(loading = plan.now.isNotEmpty())
        if (plan.now.isEmpty()) return

        loader.fetch(plan.now)
        publish(loading = false)
        fetchVisibleWaiting()
    }

    private fun fetchVisibleWaiting() {
        if (_state.value.loading || waiting.isEmpty()) return
        fetchWaiting(nextQuoteBatch(displayOrder(), waiting, lastVisible))
    }

    private fun fetchWaiting(batch: List<String>) {
        if (batch.isEmpty()) return
        waiting -= batch.toSet()
        viewModelScope.launch {
            loader.fetch(batch)
            publish()
        }
    }

    private fun publish(loading: Boolean? = null) {
        _state.value = _state.value.copy(
            items = items(),
            loading = loading ?: _state.value.loading,
            offlineSince = loader.offlineSince(symbols),
        )
    }

    private fun items(): List<CompanyQuote> =
        symbols.sorted().mapNotNull { symbol ->
            Companies.find(symbol)?.let { CompanyQuote(it, loader.quoteOf(symbol)) }
        }

    private fun displayOrder(): List<String> =
        items().sortedWith(_state.value.sort).map { it.company.symbol }
}
