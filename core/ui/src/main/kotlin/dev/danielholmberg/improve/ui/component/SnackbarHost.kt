package dev.danielholmberg.improve.ui.component

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Creates and remembers a [SnackbarHostState] for use with [ImproveSnackbarHost].
 */
@Composable
fun rememberImproveSnackbarHostState(): SnackbarHostState = remember { SnackbarHostState() }

/**
 * A themed [SnackbarHost] wrapper that applies Improve brand styling.
 *
 * Usage:
 * ```
 * val snackbarHostState = rememberImproveSnackbarHostState()
 * Scaffold(
 *     snackbarHost = { ImproveSnackbarHost(hostState = snackbarHostState) }
 * ) { ... }
 * // Show a message:
 * LaunchedEffect(event) { snackbarHostState.showSnackbar("Message") }
 * ```
 */
@Composable
fun ImproveSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                shape = MaterialTheme.shapes.small,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                actionColor = MaterialTheme.colorScheme.inversePrimary,
            )
        },
    )
}

// Re-export SnackbarResult so callers don't need an extra import.
typealias ImproveSnackbarResult = SnackbarResult
