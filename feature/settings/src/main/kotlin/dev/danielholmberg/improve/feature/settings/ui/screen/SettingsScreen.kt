package dev.danielholmberg.improve.feature.settings.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.danielholmberg.improve.feature.settings.viewmodel.SettingsUiEvent
import dev.danielholmberg.improve.feature.settings.viewmodel.SettingsViewModel
import dev.danielholmberg.improve.ui.component.ConfirmDialog
import dev.danielholmberg.improve.ui.component.ImproveSnackbarHost
import dev.danielholmberg.improve.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsUiEvent.NavigateToSignIn -> onNavigateToSignIn()
                is SettingsUiEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showSignOutDialog) {
        ConfirmDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out?",
            confirmText = "Sign Out",
            onConfirm = {
                showSignOutDialog = false
                viewModel.signOut()
            },
            onDismiss = { showSignOutDialog = false },
        )
    }

    Scaffold(
        snackbarHost = { ImproveSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
                return@Box
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                // User profile card
                UserProfileCard(
                    displayName = uiState.userProfile?.displayName
                        ?.takeIf { it.isNotBlank() } ?: "Anonymous",
                    email = uiState.userProfile?.email
                        ?.takeIf { it.isNotBlank() } ?: "",
                    photoUrl = uiState.userProfile?.photoUrl ?: "",
                    isAnonymous = uiState.userProfile?.isAnonymous ?: true,
                )

                Spacer(modifier = Modifier.size(24.dp))

                // Menu items
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    SettingsMenuItem(
                        icon = Icons.Default.Feedback,
                        label = "Send Feedback",
                        onClick = onNavigateToFeedback,
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingsMenuItem(
                        icon = Icons.Default.Policy,
                        label = "Privacy Policy",
                        onClick = onNavigateToPrivacyPolicy,
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingsMenuItem(
                        icon = Icons.Default.ExitToApp,
                        label = "Sign Out",
                        labelColor = MaterialTheme.colorScheme.error,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { showSignOutDialog = true },
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    displayName: String,
    email: String,
    photoUrl: String,
    isAnonymous: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            if (photoUrl.isNotBlank() && !isAnonymous) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = if (isAnonymous) "Anonymous" else displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (!isAnonymous && email.isNotBlank()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                } else if (isAnonymous) {
                    Text(
                        text = "Signed in anonymously",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = labelColor,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
