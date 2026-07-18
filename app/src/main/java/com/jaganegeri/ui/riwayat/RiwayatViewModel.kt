package com.jaganegeri.ui.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaganegeri.data.model.CorruptionCase
import com.jaganegeri.data.repository.CaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RiwayatUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val results: List<CorruptionCase> = emptyList(),
    val groupedResults: Map<String, List<CorruptionCase>> = emptyMap(),
    val hasSearched: Boolean = false,
    val error: String? = null
)

class RiwayatViewModel(
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiwayatUiState())
    val uiState: StateFlow<RiwayatUiState> = _uiState

    fun search(query: String, wilayah: String = "") {
        if (query.isBlank() && wilayah.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, query = query, hasSearched = true)
            try {
                val results = caseRepository.searchCases(query.trim(), wilayah.trim())

                // Group by nama koruptor
                val grouped = results.groupBy { it.namaKoruptor }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = results,
                    groupedResults = grouped
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Pencarian gagal"
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.value = RiwayatUiState()
    }
}
