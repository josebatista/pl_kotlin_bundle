package dev.josebatista.chirp

data class MainState(
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = true,
)
