package dev.josebatista.auth.domain

object EmailValidator {

    private val EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()

    fun validate(email: String): Boolean = EMAIL_PATTERN.matches(email)
}
