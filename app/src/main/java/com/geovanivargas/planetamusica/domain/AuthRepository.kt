package com.geovanivargas.planetamusica.domain

/**
 * Contrato del repositorio de autenticación.
 * La implementación real vendrá en iteraciones posteriores.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun signUp(name: String, email: String, password: String): AuthResult
    suspend fun recoverPassword(email: String): AuthResult
    fun getCurrentUserId(): String?
}
