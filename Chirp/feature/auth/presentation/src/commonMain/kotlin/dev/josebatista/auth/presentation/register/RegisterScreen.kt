package dev.josebatista.auth.presentation.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.josebatista.core.designsystem.theme.ChirpTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RegisterRoot(
    viewModel: RegisterViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RegisterScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit
) {

}

@Preview
@Composable
private fun RegisterScreenLightThemePreview() {
    ChirpTheme {
        RegisterScreen(
            state = RegisterState(),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun RegisterScreenDarkThemePreview() {
    ChirpTheme(darkTheme = true) {
        RegisterScreen(
            state = RegisterState(),
            onAction = {}
        )
    }
}
