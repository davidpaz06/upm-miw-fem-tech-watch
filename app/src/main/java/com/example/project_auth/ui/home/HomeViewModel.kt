package com.example.project_auth.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.FirestoreRepository
import com.example.project_auth.data.QuoteCache
import com.example.project_auth.data.StockRepository
import com.example.project_auth.data.toSnapshot
import com.example.project_auth.data.remote.Quote
import com.example.project_auth.domain.Companies
import com.example.project_auth.domain.Company
import com.example.project_auth.domain.Sector
import kotlinx.coroutines.Job
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
    private val cache: QuoteCache = QuoteCache.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun selectSector(sector: Sector) {
        if (sector == _state.value.sector) return
        _state.update {
            it.copy(sector = sector, items = emptyList(), loading = false, loaded = false, offlineSince = null)
        }
        load()
    }

    fun selectSort(field: SortField) = _state.update { it.copy(sort = it.sort.select(field)) }

    fun refresh() = load()

    /**
     * Shows the locally saved quotes straight away, then replaces them with fresh ones.
     * Any symbol Finnhub cannot answer keeps its saved quote, and [HomeUiState.offlineSince]
     * tells the UI that some of what it shows is old.
     */
    private fun load() {
        loadJob?.cancel()
        val companies = Companies.bySector(_state.value.sector)
        val cached = cache.load(companies.map { it.symbol })
        _state.update { state ->
            state.copy(
                loading = true,
                items = state.items.ifEmpty {
                    if (cached.isEmpty()) emptyList() else companies.map { CompanyQuote(it, cached[it.symbol]?.quote) }
                },
            )
        }
        loadJob = viewModelScope.launch {
            val fresh = repository.getQuotes(companies.map { it.symbol })
                .mapNotNull { (symbol, result) -> result.getOrNull()?.let { symbol to it } }
                .toMap()
            cache.save(fresh)
            _state.update {
                it.copy(
                    items = companies.map { company ->
                        CompanyQuote(company, fresh[company.symbol] ?: cached[company.symbol]?.quote)
                    },
                    loading = false,
                    loaded = true,
                    offlineSince = companies
                        .filter { it.symbol !in fresh }
                        .mapNotNull { cached[it.symbol]?.savedAt }
                        .minOrNull(),
                )
            }
            saveSnapshots(fresh)
        }
    }

    /** Runs on its own scope so a slow or failing write never blocks the quote list. */
    private fun saveSnapshots(quotes: Map<String, Quote>) {
        val snapshots = quotes.map { (symbol, quote) -> quote.toSnapshot(symbol) }
        viewModelScope.launch { firestore.saveSnapshots(snapshots) }
    }
}
