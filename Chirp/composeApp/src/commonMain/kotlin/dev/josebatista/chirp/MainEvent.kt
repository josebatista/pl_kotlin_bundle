package dev.josebatista.chirp

sealed interface MainEvent {
    data object OnSessionExpired : MainEvent
}
