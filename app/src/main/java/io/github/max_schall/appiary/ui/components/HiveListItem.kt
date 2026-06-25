package io.github.max_schall.appiary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.domain.model.HiveStatus
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.model.HiveListUi
import io.github.max_schall.appiary.ui.theme.Spacing
import io.github.max_schall.appiary.ui.theme.statusColors
import io.github.max_schall.appiary.ui.util.UiFormat

/** A compact, scannable hive row: name, key attributes, and next action. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HiveListItem(ui: HiveListUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hive = ui.hive
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            HiveStatusDot(hive.status)
            Column(Modifier.weight(1f).padding(start = Spacing.md)) {
                Text(
                    hive.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(
                        R.string.hive_summary,
                        stringResource(hive.queenStatus.labelRes()),
                        stringResource(hive.broodPattern.labelRes()),
                        UiFormat.relativeOrNever(hive.lastInspectionAt),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ui.nextAction?.let { rec ->
                    FlowRow(
                        Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        val c = bucketColors(rec.urgencyBucket)
                        StatusBadge(rec.title, categoryIcon(rec.category), c.container, c.onContainer)
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Status indicator: colored dot (paired with the status label in the row text). */
@Composable
fun HiveStatusDot(status: HiveStatus, modifier: Modifier = Modifier) {
    val s = MaterialTheme.statusColors
    val color = when (status) {
        HiveStatus.ACTIVE -> s.healthy
        HiveStatus.WEAK -> s.watchlist
        HiveStatus.QUEENLESS -> s.doNow
        HiveStatus.DEAD -> MaterialTheme.colorScheme.outline
    }
    Surface(color = color, shape = CircleShape, modifier = modifier.size(12.dp)) {}
}
