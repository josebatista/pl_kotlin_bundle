package dev.josebatista.chirp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import dev.josebatista.chirp.navigation.DeepLinkListener
import dev.josebatista.chirp.navigation.NavigationRoot
import dev.josebatista.core.designsystem.theme.ChirpTheme
import dev.josebatista.core.presentation.util.PreviewScreens

@Composable
@PreviewScreens
fun App() {
    val navController = rememberNavController()
    DeepLinkListener(navController)
    ChirpTheme {
        NavigationRoot(navController)
    }
}
