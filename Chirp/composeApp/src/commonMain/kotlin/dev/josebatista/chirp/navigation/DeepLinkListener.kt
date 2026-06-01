package dev.josebatista.chirp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavUri

@Composable
fun DeepLinkListener(
    navController: NavController
) {
    DisposableEffect(Unit) {
        ExternalUriHandler.listener = { uri ->
            navController.navigate(NavUri(uriString = uri))
        }

        onDispose { ExternalUriHandler.listener = null }
    }
}
