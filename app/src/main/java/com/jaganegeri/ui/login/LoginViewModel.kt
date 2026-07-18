package com.jaganegeri.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaganegeri.data.model.Profile
import com.jaganegeri.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val profile: Profile? = null,
    val showRegister: Boolean = false,
    val registerSuccess: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Username dan password harus diisi")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(username.trim(), password)
            result.fold(
                onSuccess = { profile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        profile = profile
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Username atau password salah"
                    )
                }
            )
        }
    }

    fun register(username: String, password: String, confirmPassword: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Semua field harus diisi")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Password tidak cocok")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password minimal 6 karakter")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Cek username sudah dipakai
            if (authRepository.isUsernameTaken(username.trim())) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Username sudah digunakan"
                )
                return@launch
            }

            val result = authRepository.register(username.trim(), password)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showRegister = false,
                        error = null,
                        registerSuccess = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Registrasi gagal, coba lagi"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun toggleRegister() {
        _uiState.value = _uiState.value.copy(
            showRegister = !_uiState.value.showRegister,
            error = null
        )
    }
}
