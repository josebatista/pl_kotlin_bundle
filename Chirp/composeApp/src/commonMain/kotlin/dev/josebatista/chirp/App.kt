package dev.josebatista.chirp

import androidx.compose.runtime.Composable
import dev.josebatista.auth.presentation.register.RegisterRoot
import dev.josebatista.core.designsystem.theme.ChirpTheme
import dev.josebatista.core.presentation.util.PreviewScreens

@Composable
@PreviewScreens
fun App() {
    ChirpTheme {
        RegisterRoot(
            onRegisterSuccess = {}
        )
    }
}
