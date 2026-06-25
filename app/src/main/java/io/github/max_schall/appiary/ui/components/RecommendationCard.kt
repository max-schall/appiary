package io.github.max_schall.appiary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.model.RecommendationItem
import io.github.max_schall.appiary.ui.theme.Spacing

/**
 * Operational recommendation card — labeled-inspection-sheet feel, not a promo
 * tile. A bucket-colored accent bar carries urgency, the body explains why, and
 * the actions let the beekeeper act, snooze, or dismiss without leaving Today.
 */
@Composable
fun RecommendationCard(
    item: RecommendationItem,
    onPrimaryAction: () -> Unit,
    onExplain: () -> Unit,
    onComplete: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rec = item.rec
    val colors = bucketColors(rec.urgencyBucket)
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        onClick = onExplain,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(Spacing.md)) {
            AccentBar(colors.container, Modifier.padding(end = Spacing.md, top = 2.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        categoryIcon(rec.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp).padding(end = 0.dp),
                    )
                    Text(
                        rec.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(start = Spacing.sm),
                    )
                    UrgencyBadge(rec.urgencyBucket)
                }

                Text(
                    rec.shortReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )

                val subtitle = listOfNotNull(item.hiveName, item.apiaryName).joinToString(" · ")
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    OutlinedButton(onClick = onPrimaryAction) {
                        Text(stringResource(rec.recommendedActionType.labelRes()))
                    }
                    Text(
                        stringResource(R.string.tap_for_why),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).padding(start = Spacing.xs),
                    )
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More actions")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_done)) },
                            leadingIcon = { Icon(Icons.Outlined.Check, null) },
                            onClick = { menuOpen = false; onComplete() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_snooze)) },
                            leadingIcon = { Icon(Icons.Outlined.Bedtime, null) },
                            onClick = { menuOpen = false; onSnooze() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_dismiss)) },
                            leadingIcon = { Icon(Icons.Outlined.Close, null) },
                            onClick = { menuOpen = false; onDismiss() },
                        )
                    }
                }
            }
        }
    }
}
