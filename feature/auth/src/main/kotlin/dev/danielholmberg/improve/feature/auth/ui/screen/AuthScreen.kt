package dev.danielholmberg.improve.feature.auth.ui.screen

import androidx.compose.runtime.Composable

@Composable
fun AuthScreen(
    onNavigateToMain: () -> Unit,
) {
    SignInScreen(onNavigateToMain = onNavigateToMain)
}
