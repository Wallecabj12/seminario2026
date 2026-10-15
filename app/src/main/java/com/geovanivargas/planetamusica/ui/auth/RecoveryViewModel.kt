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

data class RecoveryUiState(
    val email: String = "",
    val status: AuthStatus = AuthStatus.Idle,
) {
    val isEmailValid: Boolean get() = email.isBlank() || Validators.isEmailValid(email)
    val isFormValid: Boolean get() = Validators.isEmailValid(email)
}

// ── UiEvent ──────────────────────────────────────────────

sealed interface RecoveryUiEvent {
    data class EmailChanged(val value: String) : RecoveryUiEvent
    data object Submit : RecoveryUiEvent
    data object ClearStatus : RecoveryUiEvent
}

// ── ViewModel ────────────────────────────────────────────

class RecoveryViewModel(
    private val repo: AuthRepository = FakeAuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(RecoveryUiState())
    val state: StateFlow<RecoveryUiState> = _state.asStateFlow()

    fun onEvent(event: RecoveryUiEvent) {
        when (event) {
            is RecoveryUiEvent.EmailChanged ->
                _state.update { it.copy(email = event.value, status = AuthStatus.Idle) }

            RecoveryUiEvent.Submit -> recover()

            RecoveryUiEvent.ClearStatus ->
                _state.update { it.copy(status = AuthStatus.Idle) }
        }
    }

    private fun recover() {
        _state.update { it.copy(status = AuthStatus.Loading) }
        viewModelScope.launch {
            val result = repo.recoverPassword(_state.value.email.trim())
            _state.update {
                when (result) {
                    is AuthResult.Success -> it.copy(status = AuthStatus.Success)
                    is AuthResult.Error -> it.copy(status = AuthStatus.Error(result.message))
                }
            }
        }
    }
}
