package dev.danielholmberg.improve.feature.notes.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.feature.notes.ui.component.NoteCard
import dev.danielholmberg.improve.feature.notes.ui.component.SwipeToDismissBackground
import dev.danielholmberg.improve.feature.notes.viewmodel.ArchivedNotesUiEvent
import dev.danielholmberg.improve.feature.notes.viewmodel.ArchivedNotesViewModel
import dev.danielholmberg.improve.feature.notes.viewmodel.TagsViewModel
import dev.danielholmberg.improve.ui.component.ConfirmDialog
import dev.danielholmberg.improve.ui.component.EmptyState
import dev.danielholmberg.improve.ui.component.ImproveSearchBar
import dev.danielholmberg.improve.ui.component.ImproveSnackbarHost
import dev.danielholmberg.improve.ui.component.LoadingIndicator
import dev.danielholmberg.improve.ui.component.rememberImproveSnackbarHostState
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedNotesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ArchivedNotesViewModel = hiltViewModel(),
    tagsViewModel: TagsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tagsUiState by tagsViewModel.uiState.collectAsStateWithLifecycle()
    val allTags = tagsUiState.tags
    val snackbarHostState = rememberImproveSnackbarHostState()
    var showSearch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ArchivedNotesUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Archive",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search archived notes",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        snackbarHost = { ImproveSnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            AnimatedVisibility(
                visible = showSearch,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)),
                exit = fadeOut(tween(200)),
            ) {
                ImproveSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Search archived notes...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.notes.isEmpty() -> EmptyState(
                    message = if (uiState.searchQuery.isNotBlank()) {
                        "No archived notes match \"${uiState.searchQuery}\""
                    } else {
                        "Archive is empty."
                    },
                )
                else -> ArchivedNotesList(
                    notes = uiState.notes,
                    allTags = allTags,
                    onNoteClick = onNavigateToNoteDetail,
                    onNoteDelete = viewModel::deleteNote,
                    onNoteUnarchive = viewModel::unarchiveNote,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ArchivedNotesList(
    notes: List<Note>,
    allTags: List<Tag>,
    onNoteClick: (String) -> Unit,
    onNoteDelete: (Note) -> Unit,
    onNoteUnarchive: (Note) -> Unit,
    modifier: Modifier = Modifier,
) {
    var noteToUnarchive by remember { mutableStateOf<Note?>(null) }

    noteToUnarchive?.let { note ->
        ConfirmDialog(
            title = "Restore note",
            message = "Restore \"${note.title}\" from archive?",
            confirmText = "Restore",
            dismissText = "Cancel",
            onConfirm = {
                onNoteUnarchive(note)
                noteToUnarchive = null
            },
            onDismiss = { noteToUnarchive = null },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        items(
            items = notes,
            key = { note -> note.id },
        ) { note ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        onNoteDelete(note)
                        true
                    } else {
                        false
                    }
                },
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    SwipeToDismissBackground(
                        dismissDirection = dismissState.dismissDirection,
                        deleteColor = MaterialTheme.colorScheme.error,
                    )
                },
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .animateItemPlacement(),
            ) {
                NoteCard(
                    note = note,
                    tags = allTags,
                    onClick = { onNoteClick(note.id) },
                    onLongClick = { noteToUnarchive = note },
                )
            }
        }
    }
}
