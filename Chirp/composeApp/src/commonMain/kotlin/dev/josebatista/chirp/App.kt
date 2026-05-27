package dev.josebatista.chirp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dev.josebatista.auth.presentation.navigation.AuthGraphRoutes
import dev.josebatista.chat.presentation.chat_list.ChatListRoute
import dev.josebatista.chirp.navigation.DeepLinkListener
import dev.josebatista.chirp.navigation.NavigationRoot
import dev.josebatista.core.designsystem.theme.ChirpTheme
import dev.josebatista.core.presentation.util.PreviewScreens
import org.koin.compose.viewmodel.koinViewModel

@Composable
@PreviewScreens
fun App(
    onAuthenticationChecked: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    DeepLinkListener(navController)
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isCheckingAuth) { if (!state.isCheckingAuth) onAuthenticationChecked() }
    ChirpTheme {
        if (!state.isCheckingAuth) {
            NavigationRoot(
                navController = navController,
                startDestination = if (state.isLoggedIn) {
                    ChatListRoute
                } else {
                    AuthGraphRoutes.Graph
                }
            )
        }
    }
}
