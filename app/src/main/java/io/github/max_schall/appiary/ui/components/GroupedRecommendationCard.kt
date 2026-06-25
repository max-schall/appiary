package io.github.max_schall.appiary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.model.GroupKind
import io.github.max_schall.appiary.ui.model.RecommendationGroup
import io.github.max_schall.appiary.ui.model.RecommendationItem
import io.github.max_schall.appiary.ui.theme.Spacing

/**
 * One card consolidating every open recommendation for a single hive — or the
 * apiary-level (weather/season/nectar/compliance) items for a location. The header
 * names the subject and shows its most-urgent badge; each recommendation is a row
 * beneath with its own act/snooze/dismiss controls, so nothing is lost versus the
 * old one-card-per-item layout.
 */
@Composable
fun GroupedRecommendationCard(
    group: RecommendationGroup,
    onPrimaryAction: (RecommendationItem) -> Unit,
    onExplain: (RecommendationItem) -> Unit,
    onComplete: (RecommendationItem) -> Unit,
    onSnooze: (RecommendationItem) -> Unit,
    onDismiss: (RecommendationItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = bucketColors(group.worstBucket)

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(Spacing.md)) {
            AccentBar(colors.container, Modifier.padding(end = Spacing.md, top = 2.dp))

            Column(Modifier.weight(1f)) {
                // Header: subject name + most-urgent badge.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        groupIcon(group.kind),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        group.title ?: stringResource(R.string.group_general),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(start = Spacing.sm),
                    )
                    UrgencyBadge(group.worstBucket)
                }
                if (!group.subtitle.isNullOrBlank()) {
                    Text(
                        group.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 26.dp, top = 2.dp),
                    )
                }

                group.items.forEachIndexed { index, item ->
                    if (index == 0) {
                        HorizontalDivider(Modifier.padding(top = Spacing.sm))
                    } else {
                        HorizontalDivider(
                            Modifier.padding(start = 26.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        )
                    }
                    RecommendationRow(
                        item = item,
                        onPrimaryAction = { onPrimaryAction(item) },
                        onExplain = { onExplain(item) },
                        onComplete = { onComplete(item) },
                        onSnooze = { onSnooze(item) },
                        onDismiss = { onDismiss(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationRow(
    item: RecommendationItem,
    onPrimaryAction: () -> Unit,
    onExplain: () -> Unit,
    onComplete: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    val rec = item.rec
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        onClick = onExplain,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(top = Spacing.sm, bottom = Spacing.xs)) {
            Icon(
                categoryIcon(rec.category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp).padding(top = 2.dp),
            )
            Column(Modifier.weight(1f).padding(start = Spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        rec.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = Spacing.sm),
                    )
                    UrgencyBadge(rec.urgencyBucket)
                }
                Text(
                    rec.shortReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = Spacing.xs),
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
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_actions))
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

private fun groupIcon(kind: GroupKind): ImageVector = when (kind) {
    GroupKind.HIVE -> Icons.Outlined.Hexagon
    GroupKind.APIARY -> Icons.Outlined.Grass
    GroupKind.GENERAL -> Icons.AutoMirrored.Outlined.Assignment
}
