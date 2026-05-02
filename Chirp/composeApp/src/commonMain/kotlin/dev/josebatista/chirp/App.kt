package dev.josebatista.chirp

import androidx.compose.runtime.Composable
import dev.josebatista.chirp.navigation.NavigationRoot
import dev.josebatista.core.designsystem.theme.ChirpTheme
import dev.josebatista.core.presentation.util.PreviewScreens

@Composable
@PreviewScreens
fun App() {
    ChirpTheme {
        NavigationRoot()
    }
}
