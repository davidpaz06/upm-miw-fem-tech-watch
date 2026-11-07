package com.example.project_auth.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.ExchangeRateRepository
import com.example.project_auth.data.FirestoreRepository
import com.example.project_auth.data.PriceHistoryRepository
import com.example.project_auth.data.QuoteSnapshot
import com.example.project_auth.data.StockRepository
import com.example.project_auth.data.remote.CompanyProfile
import com.example.project_auth.domain.MarketCap
import com.example.project_auth.domain.PriceStats
import com.example.project_auth.domain.entriesToSave
import com.example.project_auth.domain.toPriceStats
import com.example.project_auth.domain.tradingCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    /** Oldest first. */
    val snapshots: List<QuoteSnapshot> = emptyList(),
    val loading: Boolean = true,
    val failed: Boolean = false,
    /** Null until it loads, and stays null if Finnhub has no profile for the symbol. */
    val profile: CompanyProfile? = null,
    val isFavourite: Boolean = false,
    val marketCap: MarketCap? = null,
) {
    val stats: PriceStats? get() = snapshots.toPriceStats()
}

class HistoryViewModel(
    private val symbol: String,
    private val firestore: FirestoreRepository = FirestoreRepository(),
    private val stocks: StockRepository = StockRepository(),
    private val priceHistory: PriceHistoryRepository = PriceHistoryRepository(),
    private val rates: ExchangeRateRepository = ExchangeRateRepository.shared,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            firestore.watchlistSymbols()
                .catch { emit(emptySet()) }
                .collect { symbols -> _state.update { it.copy(isFavourite = symbol in symbols) } }
        }
    }

    fun toggleFavourite() {
        viewModelScope.launch {
            if (_state.value.isFavourite) firestore.removeFromWatchlist(symbol) else firestore.addToWatchlist(symbol)
        }
    }

    /** Reloads everything; called each time the detail is opened because the view model outlives it. */
    fun refresh() {
        load()
        loadProfile()
    }

    /**
     * Queries the last [ENTRIES] daily prices and builds the chart from that answer, then brings
     * Firestore up to date with it. If the query fails, it falls back to what Firestore already has.
     */
    fun load() {
        _state.update { it.copy(loading = true, failed = false) }
        viewModelScope.launch {
            val history = priceHistory.getHistory(symbol, ENTRIES).getOrNull()
            if (history != null) {
                _state.update { it.copy(snapshots = history, loading = false) }
                saveToFirestore(history)
            } else {
                loadFromFirestore()
            }
        }
    }

    private suspend fun saveToFirestore(history: List<QuoteSnapshot>) {
        // If the stored entries cannot be read, writing blindly would only duplicate work.
        firestore.getSnapshots(symbol).onSuccess { stored ->
            firestore.saveSnapshots(entriesToSave(history, stored))
        }
    }

    private suspend fun loadFromFirestore() {
        firestore.getSnapshots(symbol)
            .onSuccess { stored ->
                _state.update { it.copy(snapshots = stored.takeLast(ENTRIES), loading = false) }
            }
            .onFailure { _state.update { it.copy(loading = false, failed = true) } }
    }

    private companion object {
        const val ENTRIES = PriceHistoryRepository.DEFAULT_COUNT
    }

    /** The profile is decoration, so a failure is ignored rather than shown. */
    private fun loadProfile() {
        viewModelScope.launch {
            stocks.getProfile(symbol).onSuccess { profile ->
                _state.update { it.copy(profile = profile) }
                showMarketCap(profile)
            }
        }
    }

    /**
     * Shows the market cap in its own currency straight away, then replaces it with the US
     * dollar amount once the exchange rate arrives. With no known currency nothing is shown,
     * because a number with the wrong currency is worse than none.
     */
    private suspend fun showMarketCap(profile: CompanyProfile) {
        val millions = profile.marketCap ?: return
        val currency = profile.tradingCurrency() ?: return
        if (currency == MarketCap.USD) {
            _state.update { it.copy(marketCap = MarketCap.inUsd(millions)) }
            return
        }
        _state.update { it.copy(marketCap = MarketCap.unconverted(millions, currency)) }
        rates.unitsPerUsd(currency).onSuccess { unitsPerUsd ->
            _state.update { it.copy(marketCap = MarketCap.converted(millions, currency, unitsPerUsd)) }
        }
    }
}
