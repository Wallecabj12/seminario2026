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

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    val status: AuthStatus = AuthStatus.Idle,
) {
    val isEmailValid: Boolean get() = email.isBlank() || Validators.isEmailValid(email)
    val isPasswordValid: Boolean get() = password.isBlank() || Validators.isPasswordValid(password)
    val doPasswordsMatch: Boolean
        get() = confirmPassword.isBlank() || Validators.doPasswordsMatch(password, confirmPassword)
    val isFormValid: Boolean
        get() = name.isNotBlank()
                && Validators.isEmailValid(email)
                && Validators.isPasswordValid(password)
                && Validators.doPasswordsMatch(password, confirmPassword)
                && termsAccepted
}

// ── UiEvent ──────────────────────────────────────────────

sealed interface SignUpUiEvent {
    data class NameChanged(val value: String) : SignUpUiEvent
    data class EmailChanged(val value: String) : SignUpUiEvent
    data class PasswordChanged(val value: String) : SignUpUiEvent
    data class ConfirmPasswordChanged(val value: String) : SignUpUiEvent
    data object TogglePasswordVisibility : SignUpUiEvent
    data class TermsChanged(val accepted: Boolean) : SignUpUiEvent
    data object Submit : SignUpUiEvent
    data object ClearStatus : SignUpUiEvent
}

// ── ViewModel ────────────────────────────────────────────

class SignUpViewModel(
    private val repo: AuthRepository = FakeAuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    fun onEvent(event: SignUpUiEvent) {
        when (event) {
            is SignUpUiEvent.NameChanged ->
                _state.update { it.copy(name = event.value, status = AuthStatus.Idle) }

            is SignUpUiEvent.EmailChanged ->
                _state.update { it.copy(email = event.value, status = AuthStatus.Idle) }

            is SignUpUiEvent.PasswordChanged ->
                _state.update { it.copy(password = event.value, status = AuthStatus.Idle) }

            is SignUpUiEvent.ConfirmPasswordChanged ->
                _state.update { it.copy(confirmPassword = event.value, status = AuthStatus.Idle) }

            SignUpUiEvent.TogglePasswordVisibility ->
                _state.update { it.copy(passwordVisible = !it.passwordVisible) }

            is SignUpUiEvent.TermsChanged ->
                _state.update { it.copy(termsAccepted = event.accepted, status = AuthStatus.Idle) }

            SignUpUiEvent.Submit -> signUp()

            SignUpUiEvent.ClearStatus ->
                _state.update { it.copy(status = AuthStatus.Idle) }
        }
    }

    private fun signUp() {
        _state.update { it.copy(status = AuthStatus.Loading) }
        viewModelScope.launch {
            val s = _state.value
            val result = repo.signUp(s.name.trim(), s.email.trim(), s.password)
            _state.update {
                when (result) {
                    is AuthResult.Success -> it.copy(status = AuthStatus.Success)
                    is AuthResult.Error -> it.copy(status = AuthStatus.Error(result.message))
                }
            }
        }
    }
}
