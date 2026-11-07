package com.example.project_auth.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.CachedQuote
import com.example.project_auth.data.FirestoreRepository
import com.example.project_auth.data.QuoteCache
import com.example.project_auth.data.StockRepository
import com.example.project_auth.data.remote.Quote
import com.example.project_auth.domain.Companies
import com.example.project_auth.domain.isFresh
import com.example.project_auth.ui.home.CompanyQuote
import com.example.project_auth.ui.home.SortField
import com.example.project_auth.ui.home.SortOrder
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
    private val repository: StockRepository = StockRepository(),
    private val firestore: FirestoreRepository = FirestoreRepository(),
    private val cache: QuoteCache = QuoteCache.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistUiState())
    val state: StateFlow<WatchlistUiState> = _state.asStateFlow()

    /** Fresh quotes fetched this session, so adding or removing a favourite only asks for what is missing. */
    private val quotes = mutableMapOf<String, Quote>()
    private var symbols: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            firestore.watchlistSymbols()
                .catch { emit(emptySet()) }
                .collectLatest { sync(it, forceRefresh = false) }
        }
    }

    /** Ignored while a load is running, so a second pull cannot request the same quotes again. */
    fun refresh() {
        if (_state.value.loading) return
        viewModelScope.launch { sync(symbols, forceRefresh = true) }
    }

    fun selectSort(field: SortField) {
        _state.value = _state.value.let { it.copy(sort = it.sort.select(field)) }
    }

    /**
     * Shows fresh quotes where there are any and the locally saved ones otherwise, so the
     * list is not empty while offline or while the first request is in flight.
     */
    private suspend fun sync(newSymbols: Set<String>, forceRefresh: Boolean) {
        symbols = newSymbols
        val cached = cache.load(newSymbols)
        _state.value = _state.value.copy(items = itemsFor(newSymbols, cached), loading = true)

        // A quote saved less than a minute ago is reused unless the user pulled to refresh.
        val now = System.currentTimeMillis()
        fun isCurrent(symbol: String) = symbol in quotes || cached[symbol]?.isFresh(now) == true
        val missing = newSymbols.filter { forceRefresh || !isCurrent(it) }
        val fresh = repository.getQuotes(missing)
            .mapNotNull { (symbol, result) -> result.getOrNull()?.let { symbol to it } }
            .toMap()
        quotes += fresh
        cache.save(fresh)

        _state.value = _state.value.copy(
            items = itemsFor(newSymbols, cached),
            loading = false,
            offlineSince = newSymbols
                .filter { it !in quotes && cached[it]?.isFresh(now) != true }
                .mapNotNull { cached[it]?.savedAt }
                .minOrNull(),
        )
    }

    private fun itemsFor(symbols: Set<String>, cached: Map<String, CachedQuote>): List<CompanyQuote> =
        symbols.sorted().mapNotNull { symbol ->
            Companies.find(symbol)?.let { CompanyQuote(it, quotes[symbol] ?: cached[symbol]?.quote) }
        }
}
