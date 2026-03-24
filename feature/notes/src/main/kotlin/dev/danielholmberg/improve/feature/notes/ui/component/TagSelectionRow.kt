package dev.danielholmberg.improve.feature.notes.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.ui.component.TagChip
import dev.danielholmberg.improve.ui.util.toComposeColor

@Composable
fun TagSelectionRow(
    availableTags: List<Tag>,
    selectedTagIds: Set<String>,
    onTagToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (availableTags.isEmpty()) {
        Text(
            text = "No tags available",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        availableTags.forEach { tag ->
            val isSelected = tag.id in selectedTagIds
            val bgColor = runCatching { tag.color.toComposeColor() }
                .getOrElse { MaterialTheme.colorScheme.primaryContainer }
            val txtColor = runCatching { tag.textColor.toComposeColor() }
                .getOrElse { MaterialTheme.colorScheme.onPrimaryContainer }

            val chipBgColor = if (isSelected) {
                bgColor
            } else {
                bgColor.copy(alpha = 0.35f)
            }

            TagChip(
                label = tag.label,
                backgroundColor = chipBgColor,
                textColor = if (isSelected) txtColor else txtColor.copy(alpha = 0.6f),
                onClick = { onTagToggle(tag.id) },
            )
        }
    }
}
