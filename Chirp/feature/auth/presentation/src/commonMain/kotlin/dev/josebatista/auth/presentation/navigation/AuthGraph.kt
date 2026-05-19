package dev.josebatista.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import dev.josebatista.auth.presentation.email_verification.EmailVerificationRoot
import dev.josebatista.auth.presentation.login.LoginRoot
import dev.josebatista.auth.presentation.register.RegisterRoot
import dev.josebatista.auth.presentation.register_success.RegisterSuccessRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit,
) {
    navigation<AuthGraphRoutes.Graph>(
        startDestination = AuthGraphRoutes.Login
    ) {
        composable<AuthGraphRoutes.Login> {
            LoginRoot(
                onLoginSuccess = onLoginSuccess,
                onForgotPasswordClick = { navController.navigate(route = AuthGraphRoutes.ForgotPassword) },
                onCreateAccountClick = {
                    navController.navigate(route = AuthGraphRoutes.Register) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable<AuthGraphRoutes.Register> {
            RegisterRoot(
                onRegisterSuccess = {
                    navController.navigate(route = AuthGraphRoutes.RegisterSuccess(email = it))
                },
                onLoginClick = {
                    navController.navigate(route = AuthGraphRoutes.Login) {
                        popUpTo(route = AuthGraphRoutes.Register) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable<AuthGraphRoutes.RegisterSuccess> {
            RegisterSuccessRoot(
                onLoginClick = {
                    navController.navigate(route = AuthGraphRoutes.Login) {
                        popUpTo<AuthGraphRoutes.RegisterSuccess> {
                            inclusive = true
                        }
                    }
                }
            )
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
            EmailVerificationRoot(
                onLoginClick = {
                    navController.navigate(route = AuthGraphRoutes.Login) {
                        popUpTo<AuthGraphRoutes.EmailVerification> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onCloseClick = {
                    navController.navigate(route = AuthGraphRoutes.Login) {
                        popUpTo<AuthGraphRoutes.EmailVerification> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
