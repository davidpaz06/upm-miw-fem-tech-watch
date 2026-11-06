package com.example.project_auth.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.FirestoreRepository
import com.example.project_auth.data.QuoteSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    /** Oldest first. */
    val snapshots: List<QuoteSnapshot> = emptyList(),
    val loading: Boolean = true,
    val failed: Boolean = false,
)

class HistoryViewModel(
    private val symbol: String,
    private val firestore: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = HistoryUiState(loading = true)
        viewModelScope.launch {
            firestore.getSnapshots(symbol)
                .onSuccess { _state.value = HistoryUiState(snapshots = it, loading = false) }
                .onFailure { _state.value = HistoryUiState(loading = false, failed = true) }
        }
    }
}
