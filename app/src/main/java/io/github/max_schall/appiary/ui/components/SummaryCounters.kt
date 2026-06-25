package io.github.max_schall.appiary.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.theme.Spacing

data class CounterData(
    val doNow: Int,
    val dueSoon: Int,
    val watchlist: Int,
    val healthy: Int,
)

/** The four-up status summary at the top of Today. Tapping toggles a bucket filter. */
@Composable
fun SummaryCounters(
    counts: CounterData,
    selected: UrgencyBucket?,
    onSelect: (UrgencyBucket?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        CounterCell(UrgencyBucket.DO_NOW, counts.doNow, selected, onSelect, Modifier.weight(1f))
        CounterCell(UrgencyBucket.DUE_SOON, counts.dueSoon, selected, onSelect, Modifier.weight(1f))
        CounterCell(UrgencyBucket.WATCHLIST, counts.watchlist, selected, onSelect, Modifier.weight(1f))
        CounterCell(UrgencyBucket.HEALTHY, counts.healthy, selected, onSelect, Modifier.weight(1f))
    }
}

@Composable
private fun CounterCell(
    bucket: UrgencyBucket,
    count: Int,
    selected: UrgencyBucket?,
    onSelect: (UrgencyBucket?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = bucketColors(bucket)
    val isSelected = selected == bucket
    Surface(
        onClick = { onSelect(if (isSelected) null else bucket) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = if (isSelected) BorderStroke(2.dp, colors.container) else null,
        modifier = modifier,
    ) {
        Column(
            Modifier.padding(vertical = Spacing.md, horizontal = Spacing.sm),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            Surface(color = colors.container, shape = MaterialTheme.shapes.small) {
                Icon(
                    bucketIcon(bucket),
                    contentDescription = null,
                    tint = colors.onContainer,
                    modifier = Modifier.padding(4.dp).size(16.dp),
                )
            }
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                stringResource(bucket.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
