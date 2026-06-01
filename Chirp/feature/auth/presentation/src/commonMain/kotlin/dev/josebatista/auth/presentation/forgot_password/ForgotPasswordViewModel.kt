package dev.josebatista.auth.presentation.forgot_password

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.josebatista.auth.domain.EmailValidator
import dev.josebatista.core.domain.auth.AuthService
import dev.josebatista.core.domain.util.onFailure
import dev.josebatista.core.domain.util.onSuccess
import dev.josebatista.core.presentation.util.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authService: AuthService
) : ViewModel() {
    private var hasLoadedInitialDate = false
    private val isEmailValidFlow = snapshotFlow { state.value.emailTextFieldState.text.toString() }
        .map { email -> EmailValidator.validate(email) }
        .distinctUntilChanged()
    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialDate) {
                observeValidationState()
                hasLoadedInitialDate = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ForgotPasswordState()
        )

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            ForgotPasswordAction.OnSubmitClick -> submitForgotPasswordRequest()
        }
    }

    private fun observeValidationState() {
        isEmailValidFlow.onEach { isEmailValid ->
            _state.update { it.copy(canSubmit = isEmailValid) }
        }.launchIn(viewModelScope)
    }

    private fun submitForgotPasswordRequest() {
        if (state.value.isLoading || !state.value.canSubmit) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isEmailSentSuccessfully = false,
                    errorText = null,
                    isLoading = true
                )
            }
            authService
                .forgotPassword(state.value.emailTextFieldState.text.toString())
                .onSuccess {
                    _state.update { it.copy(isEmailSentSuccessfully = true, isLoading = false) }
                }.onFailure { error ->
                    _state.update { it.copy(errorText = error.toUiText(), isLoading = false) }
                }
        }
    }
}
