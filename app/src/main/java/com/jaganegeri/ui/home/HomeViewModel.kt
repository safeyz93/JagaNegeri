package com.jaganegeri.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaganegeri.data.repository.CaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val casesPerMonth: Map<Int, Int> = emptyMap(),  // month -> count
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
)

class HomeViewModel(
    private val userId: String,
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val perMonth = caseRepository.getCasesPerMonth(userId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                casesPerMonth = perMonth
            )
        }
    }
}
