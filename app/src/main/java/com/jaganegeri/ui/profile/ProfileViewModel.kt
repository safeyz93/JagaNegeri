package com.jaganegeri.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaganegeri.data.repository.CaseRepository
import com.jaganegeri.data.repository.ValidationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val totalInput: Int = 0,
    val terverifikasi: Int = 0,
    val menunggu: Int = 0,
    val ditolak: Int = 0,
    val totalVote: Int = 0,
    val approveVote: Int = 0,
    val tolakVote: Int = 0,
    val queueCount: Int = 0,
    val error: String? = null
)

class ProfileViewModel(
    private val userId: String,
    private val caseRepository: CaseRepository,
    private val validationRepository: ValidationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val allCases = caseRepository.getAllCases(userId)
                val queue = validationRepository.getValidationQueue(userId)
                val myVotes = validationRepository.getMyVotes(userId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalInput = allCases.size,
                    terverifikasi = allCases.count { it.statusVerifikasi == "terverifikasi" },
                    menunggu = allCases.count { it.statusVerifikasi == "menunggu" },
                    ditolak = allCases.count { it.statusVerifikasi == "ditolak" },
                    totalVote = myVotes.size,
                    approveVote = myVotes.count { it.keputusan == "approve" },
                    tolakVote = myVotes.count { it.keputusan == "tolak" },
                    queueCount = queue.size
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
