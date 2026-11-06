package com.example.project_auth.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.FirestoreRepository
import com.example.project_auth.domain.SectorSummary
import com.example.project_auth.domain.compareSectors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatsUiState(
    val summaries: List<SectorSummary> = emptyList(),
    val loading: Boolean = true,
    val failed: Boolean = false,
)

class StatsViewModel(
    private val firestore: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = StatsUiState(loading = true)
        viewModelScope.launch {
            firestore.getAllSnapshots()
                .onSuccess { _state.value = StatsUiState(compareSectors(it), loading = false) }
                .onFailure { _state.value = StatsUiState(loading = false, failed = true) }
        }
    }
}
