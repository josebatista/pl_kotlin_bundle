package io.github.josebatista.chirp.domain.exception

class InvalidTokenException(override val message: String? = null) : RuntimeException(message ?: "Invalid token.")
