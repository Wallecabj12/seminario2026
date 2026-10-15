package com.geovanivargas.planetamusica.domain

/**
 * Validadores puros sin dependencias de Android.
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isEmailValid(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isPasswordValid(password: String): Boolean = password.length >= 6

    fun doPasswordsMatch(password: String, confirmation: String): Boolean =
        password == confirmation && password.isNotEmpty()
}
