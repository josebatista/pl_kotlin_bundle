package dev.josebatista.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.error_account_exists
import chirp.feature.auth.presentation.generated.resources.error_invalid_email
import chirp.feature.auth.presentation.generated.resources.error_invalid_password
import dev.josebatista.auth.domain.EmailValidator
import dev.josebatista.core.domain.auth.AuthService
import dev.josebatista.core.domain.util.DataError
import dev.josebatista.core.domain.util.onFailure
import dev.josebatista.core.domain.util.onSuccess
import dev.josebatista.core.domain.validation.PasswordValidator
import dev.josebatista.core.presentation.util.UiText
import dev.josebatista.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authService: AuthService
) : ViewModel() {
    private val eventChannel = Channel<RegisterEvent>()
    val events = eventChannel.receiveAsFlow()
    private var hasLoadedInitialData = false
    private val _state = MutableStateFlow(RegisterState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                // load data
                hasLoadedInitialData = true
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RegisterState()
        )

    fun onAction(action: RegisterAction) {
        when (action) {
            RegisterAction.OnLoginClick -> Unit
            RegisterAction.OnRegisterClick -> register()
            RegisterAction.OnTogglePasswordVisibilityClick -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            else -> Unit // TODO: Handle actions
        }
    }

    private fun register() {
        if (!validateFormInputs()) return
        viewModelScope.launch {
            _state.update { it.copy(isRegistering = true) }
            val username = state.value.usernameTextState.text.toString()
            val email = state.value.emailTextState.text.toString()
            val password = state.value.passwordTextState.text.toString()
            authService
                .register(
                    username = username,
                    email = email,
                    password = password,
                )
                .onSuccess { _state.update { it.copy(isRegistering = false) } }
                .onFailure { error ->
                    val registrationError = when (error) {
                        DataError.Remote.CONFLICT -> UiText.Resource(Res.string.error_account_exists)
                        else -> error.toUiText()
                    }
                    _state.update {
                        it.copy(
                            isRegistering = false,
                            registrationError = registrationError
                        )
                    }
                }
        }
    }

    private fun clearAllTextFieldErrors() {
        _state.update {
            it.copy(
                emailError = null,
                passwordError = null,
                usernameError = null,
                registrationError = null
            )
        }
    }

    private fun validateFormInputs(): Boolean {
        clearAllTextFieldErrors()
        val currentState = state.value
        val email = currentState.emailTextState.text.toString()
        val password = currentState.passwordTextState.text.toString()
        val username = currentState.usernameTextState.text.toString()
        val isEmailValid = EmailValidator.validate(email)
        val isPasswordValid = PasswordValidator.validate(password).isValidPassword
        val isUsernameValid = username.length in 3..20
        val emailError = if (!isEmailValid) {
            UiText.Resource(Res.string.error_invalid_email)
        } else null
        val passwordError = if (!isPasswordValid) {
            UiText.Resource(Res.string.error_invalid_password)
        } else null
        val usernameError = if (!isUsernameValid) {
            UiText.Resource(Res.string.error_invalid_password)
        } else null
        _state.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError,
                usernameError = usernameError
            )
        }
        return isEmailValid && isPasswordValid && isUsernameValid
    }
}
