package com.geovanivargas.planetamusica.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geovanivargas.planetamusica.data.FakeAuthRepository
import com.geovanivargas.planetamusica.domain.AuthRepository
import com.geovanivargas.planetamusica.domain.AuthResult
import com.geovanivargas.planetamusica.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UiState ──────────────────────────────────────────────

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val status: AuthStatus = AuthStatus.Idle,
) {
    val isEmailValid: Boolean get() = email.isBlank() || Validators.isEmailValid(email)
    val isFormValid: Boolean
        get() = Validators.isEmailValid(email) && Validators.isPasswordValid(password)
}

// ── UiEvent ──────────────────────────────────────────────

sealed interface LoginUiEvent {
    data class EmailChanged(val value: String) : LoginUiEvent
    data class PasswordChanged(val value: String) : LoginUiEvent
    data object TogglePasswordVisibility : LoginUiEvent
    data object Submit : LoginUiEvent
    data object ClearStatus : LoginUiEvent
}

// ── ViewModel ────────────────────────────────────────────

class LoginViewModel(
    private val repo: AuthRepository = FakeAuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.EmailChanged ->
                _state.update { it.copy(email = event.value, status = AuthStatus.Idle) }

            is LoginUiEvent.PasswordChanged ->
                _state.update { it.copy(password = event.value, status = AuthStatus.Idle) }

            LoginUiEvent.TogglePasswordVisibility ->
                _state.update { it.copy(passwordVisible = !it.passwordVisible) }

            LoginUiEvent.Submit -> login()

            LoginUiEvent.ClearStatus ->
                _state.update { it.copy(status = AuthStatus.Idle) }
        }
    }

    private fun login() {
        _state.update { it.copy(status = AuthStatus.Loading) }
        viewModelScope.launch {
            val result = repo.login(_state.value.email.trim(), _state.value.password)
            _state.update {
                when (result) {
                    is AuthResult.Success -> it.copy(status = AuthStatus.Success)
                    is AuthResult.Error -> it.copy(status = AuthStatus.Error(result.message))
                }
            }
        }
    }
}
