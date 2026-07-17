package com.jaganegeri.ui.addcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaganegeri.data.model.CorruptionCase
import com.jaganegeri.data.repository.CaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddCaseUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val savedCaseId: String? = null
)

class AddCaseViewModel(
    private val userId: String,
    private val selectedDate: String,
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCaseUiState())
    val uiState: StateFlow<AddCaseUiState> = _uiState

    fun save(
        namaKoruptor: String,
        jabatan: String,
        wilayah: String,
        statusHukum: String,
        tanggal: String,
        sumberBerita: String,
        deskripsi: String
    ) {
        // Validasi
        if (namaKoruptor.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Nama koruptor harus diisi")
            return
        }
        if (jabatan.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Jabatan harus diisi")
            return
        }
        if (wilayah.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Wilayah harus diisi")
            return
        }
        if (statusHukum.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Status hukum harus dipilih")
            return
        }
        if (tanggal.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Tanggal harus diisi")
            return
        }
        if (sumberBerita.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Sumber berita wajib diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val kasus = CorruptionCase(
                userId = userId,
                namaKoruptor = namaKoruptor.trim(),
                jabatan = jabatan.trim(),
                wilayah = wilayah.trim(),
                statusHukum = statusHukum,
                tanggalPengumuman = tanggal,
                sumberBerita = sumberBerita.trim(),
                deskripsi = deskripsi.trim(),
                statusVerifikasi = "menunggu"
            )

            val result = caseRepository.insertCase(kasus)
            result.fold(
                onSuccess = { id ->
                    _uiState.value = AddCaseUiState(
                        isLoading = false,
                        success = true,
                        savedCaseId = id
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal menyimpan: ${e.message ?: "Coba lagi"}"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
