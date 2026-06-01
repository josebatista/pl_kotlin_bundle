package dev.josebatista.auth.presentation.login

sealed interface LoginEvent {
    data object Success : LoginEvent
}
