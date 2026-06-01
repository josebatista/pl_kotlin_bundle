package dev.josebatista.chirp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.josebatista.auth.presentation.navigation.AuthGraphRoutes
import dev.josebatista.auth.presentation.navigation.authGraph
import dev.josebatista.chat.presentation.chat_list.ChatListRoute
import dev.josebatista.chat.presentation.chat_list.ChatListScreenRoot

@Composable
fun NavigationRoot(
    navController: NavHostController,
    startDestination: Any
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authGraph(
            navController = navController,
            onLoginSuccess = {
                navController.navigate(ChatListRoute) {
                    popUpTo(AuthGraphRoutes.Graph) {
                        inclusive = true
                    }
                }
            }
        )
        composable<ChatListRoute> {
            ChatListScreenRoot()
        }
    }
}
