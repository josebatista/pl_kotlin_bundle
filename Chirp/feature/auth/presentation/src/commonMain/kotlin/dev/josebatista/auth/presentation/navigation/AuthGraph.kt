package dev.josebatista.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import dev.josebatista.auth.presentation.email_verification.EmailVerificationRoot
import dev.josebatista.auth.presentation.register.RegisterRoot
import dev.josebatista.auth.presentation.register_success.RegisterSuccessRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit,
) {
    navigation<AuthGraphRoutes.Graph>(
        startDestination = AuthGraphRoutes.Register
    ) {
        composable<AuthGraphRoutes.Register> {
            RegisterRoot(
                onRegisterSuccess = {
                    navController.navigate(route = AuthGraphRoutes.RegisterSuccess(email = it))
                }
            )
        }
        composable<AuthGraphRoutes.RegisterSuccess> {
            RegisterSuccessRoot()
        }
        composable<AuthGraphRoutes.EmailVerification>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://chirp.josebatista.dev/api/auth/verify?token={token}"
                },
                navDeepLink {
                    uriPattern = "chirp://chirp.josebatista.dev/api/auth/verify?token={token}"
                },
            )
        ) {
            EmailVerificationRoot()
        }
    }
}
