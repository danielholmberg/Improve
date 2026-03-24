package dev.danielholmberg.improve.feature.notes.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.feature.notes.ui.component.NoteCard
import dev.danielholmberg.improve.feature.notes.ui.component.SwipeToDismissBackground
import dev.danielholmberg.improve.feature.notes.viewmodel.NotesUiEvent
import dev.danielholmberg.improve.feature.notes.viewmodel.NotesViewModel
import dev.danielholmberg.improve.feature.notes.viewmodel.TagsViewModel
import dev.danielholmberg.improve.ui.component.EmptyState
import dev.danielholmberg.improve.ui.component.ImproveSearchBar
import dev.danielholmberg.improve.ui.component.ImproveSnackbarHost
import dev.danielholmberg.improve.ui.component.LoadingIndicator
import dev.danielholmberg.improve.ui.component.rememberImproveSnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToAddNote: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit,
    onNavigateToArchive: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
    tagsViewModel: TagsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tagsUiState by tagsViewModel.uiState.collectAsStateWithLifecycle()
    val allTags = tagsUiState.tags
    val snackbarHostState = rememberImproveSnackbarHostState()
    var showSearch by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NotesUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is NotesUiEvent.NavigateToAddNote -> onNavigateToAddNote()
                is NotesUiEvent.NavigateToNoteDetail -> onNavigateToNoteDetail(event.noteId)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Improve",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search notes",
                        )
                    }
                    IconButton(onClick = onNavigateToArchive) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "View archived notes",
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export notes") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Upload,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.exportNotes()
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddNote,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add note",
                )
            }
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
                    placeholder = "Search notes...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.notes.isEmpty() -> EmptyState(
                    message = if (uiState.searchQuery.isNotBlank()) {
                        "No notes match \"${uiState.searchQuery}\""
                    } else {
                        "No notes yet.\nTap + to create your first note."
                    },
                )
                else -> NotesList(
                    notes = uiState.notes,
                    allTags = allTags,
                    onNoteClick = onNavigateToNoteDetail,
                    onNoteArchive = viewModel::archiveNote,
                    onNoteStarToggle = viewModel::toggleStar,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun NotesList(
    notes: List<Note>,
    allTags: List<Tag>,
    onNoteClick: (String) -> Unit,
    onNoteArchive: (Note) -> Unit,
    onNoteStarToggle: (Note) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        onNoteArchive(note)
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
                        archiveColor = MaterialTheme.colorScheme.tertiary,
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
                    onLongClick = { onNoteStarToggle(note) },
                )
            }
        }
    }
}
