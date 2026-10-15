package com.geovanivargas.planetamusica.domain

/**
 * Resultado genérico de operaciones de autenticación.
 */
sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val message: String) : AuthResult
}
