package dev.danielholmberg.improve.feature.notes.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissBackground(
    dismissDirection: SwipeToDismissBoxValue,
    modifier: Modifier = Modifier,
    archiveColor: Color = Color(0xFF4CAF50),
    deleteColor: Color = Color(0xFFF44336),
) {
    val (backgroundColor, icon, contentDescription) = when (dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> Triple(archiveColor, Icons.Default.Archive, "Archive")
        SwipeToDismissBoxValue.StartToEnd -> Triple(deleteColor, Icons.Default.Delete, "Delete")
        SwipeToDismissBoxValue.Settled -> Triple(Color.Transparent, null, null)
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (dismissDirection) {
            SwipeToDismissBoxValue.EndToStart -> Arrangement.End
            else -> Arrangement.Start
        },
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        } else {
            Spacer(modifier = Modifier.fillMaxSize())
        }
    }
}
