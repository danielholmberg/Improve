package dev.danielholmberg.improve.feature.notes.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.feature.notes.viewmodel.NotesViewModel
import dev.danielholmberg.improve.feature.notes.viewmodel.TagsViewModel
import dev.danielholmberg.improve.ui.component.EmptyState
import dev.danielholmberg.improve.ui.component.ImproveSnackbarHost
import dev.danielholmberg.improve.ui.component.LoadingIndicator
import dev.danielholmberg.improve.ui.component.TagChip
import dev.danielholmberg.improve.ui.component.rememberImproveSnackbarHostState
import dev.danielholmberg.improve.ui.util.toComposeColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
    tagsViewModel: TagsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tagsUiState by tagsViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = rememberImproveSnackbarHostState()

    val note = uiState.notes.find { it.id == noteId }
    val noteTags = if (note != null) {
        tagsUiState.tags.filter { tag -> note.tags.containsKey(tag.id) }
    } else {
        emptyList()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = note?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                    if (note != null) {
                        Icon(
                            imageVector = if (note.isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (note.isStarred) "Starred" else "Not starred",
                            tint = if (note.isStarred) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            },
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        IconButton(onClick = { onNavigateToEdit(noteId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit note",
                            )
                        }
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
        when {
            uiState.isLoading -> LoadingIndicator()
            note == null -> EmptyState(
                message = "Note not found.",
                modifier = Modifier.padding(paddingValues),
            )
            else -> NoteDetailContent(
                note = note,
                noteTags = noteTags,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteDetailContent(
    note: Note,
    noteTags: List<Tag>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (noteTags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                noteTags.forEach { tag ->
                    val bgColor = runCatching { tag.color.toComposeColor() }
                        .getOrElse { MaterialTheme.colorScheme.primaryContainer }
                    val txtColor = runCatching { tag.textColor.toComposeColor() }
                        .getOrElse { MaterialTheme.colorScheme.onPrimaryContainer }
                    TagChip(
                        label = tag.label,
                        backgroundColor = bgColor,
                        textColor = txtColor,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (note.info.isNotBlank()) {
            Text(
                text = note.info,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (note.vipImages.isNotEmpty()) {
            Text(
                text = "Images",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(
                    items = note.vipImages,
                    key = { it },
                ) { imageUri ->
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Note image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (note.updated.isNotBlank()) {
            Text(
                text = "Last updated: ${note.updated}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
