package com.jaganegeri.ui.validation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaganegeri.data.model.CorruptionCase
import com.jaganegeri.data.repository.ValidationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ValidationUiState(
    val isLoading: Boolean = false,
    val queue: List<CorruptionCase> = emptyList(),
    val loadingCaseId: String? = null,
    val successMessage: String? = null,
    val error: String? = null
)

class ValidationQueueViewModel(
    private val userId: String,
    private val validationRepository: ValidationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ValidationUiState())
    val uiState: StateFlow<ValidationUiState> = _uiState

    init {
        loadQueue()
    }

    fun loadQueue() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val queue = validationRepository.getValidationQueue(userId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                queue = queue
            )
        }
    }

    fun approve(caseId: String) {
        submitVote(caseId, "approve")
    }

    fun tolak(caseId: String) {
        submitVote(caseId, "tolak")
    }

    private fun submitVote(caseId: String, keputusan: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingCaseId = caseId)
            val result = validationRepository.vote(caseId, userId, keputusan)
            result.fold(
                onSuccess = {
                    // Hapus dari queue
                    val newQueue = _uiState.value.queue.filter { it.id != caseId }
                    _uiState.value = _uiState.value.copy(
                        queue = newQueue,
                        loadingCaseId = null,
                        successMessage = if (keputusan == "approve") "✅ Berhasil approve" else "❌ Berkas ditolak"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        loadingCaseId = null,
                        error = "Gagal: ${e.message ?: "Coba lagi"}"
                    )
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }
}
