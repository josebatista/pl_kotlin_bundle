package dev.josebatista.core.domain.auth

data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)
