package com.example.project_auth.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.FirestoreRepository
import com.example.project_auth.data.StockRepository
import com.example.project_auth.data.remote.Quote
import com.example.project_auth.domain.Companies
import com.example.project_auth.ui.home.CompanyQuote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val items: List<CompanyQuote> = emptyList(),
    val loading: Boolean = true,
)

class WatchlistViewModel(
    private val repository: StockRepository = StockRepository(),
    private val firestore: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistUiState())
    val state: StateFlow<WatchlistUiState> = _state.asStateFlow()

    /** Quotes already fetched, so adding or removing a favourite only asks for what is missing. */
    private val quotes = mutableMapOf<String, Quote?>()
    private var symbols: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            firestore.watchlistSymbols()
                .catch { emit(emptySet()) }
                .collectLatest { sync(it, forceRefresh = false) }
        }
    }

    fun refresh() {
        viewModelScope.launch { sync(symbols, forceRefresh = true) }
    }

    fun remove(symbol: String) {
        viewModelScope.launch { firestore.removeFromWatchlist(symbol) }
    }

    private suspend fun sync(newSymbols: Set<String>, forceRefresh: Boolean) {
        symbols = newSymbols
        _state.value = _state.value.copy(loading = true)
        val missing = newSymbols.filter { forceRefresh || it !in quotes }
        repository.getQuotes(missing).forEach { (symbol, result) -> quotes[symbol] = result.getOrNull() }
        _state.value = WatchlistUiState(
            items = newSymbols.sorted().mapNotNull { symbol ->
                Companies.find(symbol)?.let { CompanyQuote(it, quotes[symbol]) }
            },
            loading = false,
        )
    }
}
