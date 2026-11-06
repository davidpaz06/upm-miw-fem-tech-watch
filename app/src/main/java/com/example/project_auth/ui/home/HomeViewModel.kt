package com.example.project_auth.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.data.StockRepository
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
) {
    val failed: Boolean get() = loaded && items.none { it.quote != null }
}

class HomeViewModel(
    private val repository: StockRepository = StockRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun selectSector(sector: Sector) {
        if (sector == _state.value.sector) return
        _state.value = HomeUiState(sector = sector)
        load()
    }

    fun refresh() = load()

    private fun load() {
        loadJob?.cancel()
        _state.update { it.copy(loading = true) }
        loadJob = viewModelScope.launch {
            val companies = Companies.bySector(_state.value.sector)
            val quotes = repository.getQuotes(companies.map { it.symbol })
            _state.update {
                it.copy(
                    items = companies.map { company ->
                        CompanyQuote(company, quotes[company.symbol]?.getOrNull())
                    },
                    loading = false,
                    loaded = true,
                )
            }
        }
    }
}
