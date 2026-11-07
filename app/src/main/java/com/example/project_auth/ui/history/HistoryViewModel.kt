package com.example.project_auth.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.FirestoreRepository
import com.example.project_auth.data.QuoteSnapshot
import com.example.project_auth.data.StockRepository
import com.example.project_auth.data.remote.CompanyProfile
import com.example.project_auth.domain.PriceStats
import com.example.project_auth.domain.toPriceStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    /** Oldest first. */
    val snapshots: List<QuoteSnapshot> = emptyList(),
    val loading: Boolean = true,
    val failed: Boolean = false,
    /** Null until it loads, and stays null if Finnhub has no profile for the symbol. */
    val profile: CompanyProfile? = null,
) {
    val stats: PriceStats? get() = snapshots.toPriceStats()
}

class HistoryViewModel(
    private val symbol: String,
    private val firestore: FirestoreRepository = FirestoreRepository(),
    private val stocks: StockRepository = StockRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    /** Reloads everything; called each time the detail is opened because the view model outlives it. */
    fun refresh() {
        load()
        loadProfile()
    }

    fun load() {
        _state.update { it.copy(loading = true, failed = false) }
        viewModelScope.launch {
            firestore.getSnapshots(symbol)
                .onSuccess { snapshots ->
                    _state.update { it.copy(snapshots = snapshots, loading = false) }
                }
                .onFailure { _state.update { it.copy(loading = false, failed = true) } }
        }
    }

    /** The profile is decoration, so a failure is ignored rather than shown. */
    private fun loadProfile() {
        viewModelScope.launch {
            stocks.getProfile(symbol).onSuccess { profile ->
                _state.update { it.copy(profile = profile) }
            }
        }
    }
}
