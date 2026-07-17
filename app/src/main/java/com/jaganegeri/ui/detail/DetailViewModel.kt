package com.jaganegeri.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaganegeri.data.model.CorruptionCase
import com.jaganegeri.data.model.Validation
import com.jaganegeri.data.repository.CaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = false,
    val case: CorruptionCase? = null,
    val validations: List<Validation> = emptyList(),
    val approveCount: Int = 0,
    val tolakCount: Int = 0,
    val error: String? = null
)

class DetailViewModel(
    private val caseId: String,
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val case = caseRepository.getCaseById(caseId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    case = case
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
