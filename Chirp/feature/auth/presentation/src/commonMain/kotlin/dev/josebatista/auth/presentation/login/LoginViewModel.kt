package dev.josebatista.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class LoginViewModel : ViewModel() {
    private var hasLoadedInitialData: Boolean = false
    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoginState()
        )

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnLoginClick -> {}
            LoginAction.OnTogglePasswordVisibility -> {}
            else -> Unit
        }
    }
}
