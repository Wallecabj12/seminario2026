package com.geovanivargas.planetamusica.ui.auth

/**
 * Estados posibles de cualquier pantalla de autenticación.
 */
sealed interface AuthStatus {
    data object Idle : AuthStatus
    data object Loading : AuthStatus
    data object Success : AuthStatus
    data class Error(val message: String) : AuthStatus
}
