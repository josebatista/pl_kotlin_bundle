package dev.josebatista.auth.presentation.email_verification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.josebatista.core.domain.auth.AuthService
import dev.josebatista.core.domain.util.onFailure
import dev.josebatista.core.domain.util.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmailVerificationViewModel(
    private val authService: AuthService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var hasLoadedInitialDate = false
    private val token = savedStateHandle.get<String>("token")
    private val _state = MutableStateFlow(EmailVerificationState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialDate) {
                verifyEmail()
                hasLoadedInitialDate = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EmailVerificationState()
        )

    // NO-OP: Actions are purely for navigation
    fun onAction(action: EmailVerificationAction) = Unit

    private fun verifyEmail() {
        viewModelScope.launch {
            _state.update { it.copy(isVerifying = true) }
            authService
                .verifyEmail(token = token ?: "Invalid Token")
                .onSuccess { _state.update { it.copy(isVerifying = false, isVerified = true) } }
                .onFailure { _state.update { it.copy(isVerifying = false, isVerified = false) } }
        }
    }
}
