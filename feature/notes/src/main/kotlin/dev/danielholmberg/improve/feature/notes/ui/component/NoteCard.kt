package dev.danielholmberg.improve.feature.notes.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.ui.component.TagChip
import dev.danielholmberg.improve.ui.util.toComposeColor

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: Note,
    tags: List<Tag>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val noteTags = tags.filter { tag -> note.tags.containsKey(tag.id) }
    val containerColor = if (note.isStarred) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (note.isStarred) 4.dp else 1.dp,
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.vipImages.isNotEmpty()) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Has images",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd),
                                containerColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Text(
                                    text = note.vipImages.size.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(
                        imageVector = if (note.isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (note.isStarred) "Starred" else "Not starred",
                        modifier = Modifier.size(20.dp),
                        tint = if (note.isStarred) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            if (note.info.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.info,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (noteTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
            }
        }
    }
}
