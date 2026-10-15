package com.geovanivargas.planetamusica.data

import com.geovanivargas.planetamusica.domain.AuthRepository
import com.geovanivargas.planetamusica.domain.AuthResult
import kotlinx.coroutines.delay

/**
 * Implementación fake con delay simulado. No almacena datos reales.
 * Simula éxito siempre excepto para el email "error@test.com" (para probar errores).
 */
class FakeAuthRepository : AuthRepository {

    companion object {
        private const val SIMULATED_DELAY_MS = 1_500L
        private const val ERROR_TEST_EMAIL = "error@test.com"
        @Volatile
        var currentUserSession: String? = null
    }

    override fun getCurrentUserId(): String? = currentUserSession

    override suspend fun login(email: String, password: String): AuthResult {
        delay(SIMULATED_DELAY_MS)
        return if (email.equals(ERROR_TEST_EMAIL, ignoreCase = true)) {
            AuthResult.Error("Credenciales incorrectas. Verifica tu email y contraseña.")
        } else {
            currentUserSession = email.trim()
            AuthResult.Success
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResult {
        delay(SIMULATED_DELAY_MS)
        return if (email.equals(ERROR_TEST_EMAIL, ignoreCase = true)) {
            AuthResult.Error("Este email ya está registrado.")
        } else {
            currentUserSession = email.trim()
            AuthResult.Success
        }
    }

    override suspend fun recoverPassword(email: String): AuthResult {
        delay(SIMULATED_DELAY_MS)
        return if (email.equals(ERROR_TEST_EMAIL, ignoreCase = true)) {
            AuthResult.Error("No encontramos una cuenta con ese email.")
        } else {
            AuthResult.Success
        }
    }
}
