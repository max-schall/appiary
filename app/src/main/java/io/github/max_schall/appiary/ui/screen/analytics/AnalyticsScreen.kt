package io.github.max_schall.appiary.ui.screen.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.domain.analytics.AnalyticsComputer
import io.github.max_schall.appiary.domain.analytics.AnalyticsData
import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.components.BarChart
import io.github.max_schall.appiary.ui.components.BarDatum
import io.github.max_schall.appiary.ui.components.EmptyState
import io.github.max_schall.appiary.ui.components.ScatterPoint
import io.github.max_schall.appiary.ui.components.SectionHeader
import io.github.max_schall.appiary.ui.components.ThresholdLine
import io.github.max_schall.appiary.ui.components.TrendScatter
import io.github.max_schall.appiary.ui.theme.Spacing
import io.github.max_schall.appiary.ui.theme.statusColors
import io.github.max_schall.appiary.ui.util.UiFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val data by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.analytics_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            },
        )

        val d = data
        when {
            d == null -> Unit // initial load
            d.isEmpty -> EmptyState(
                icon = Icons.Outlined.Insights,
                title = stringResource(R.string.analytics_empty_title),
                subtitle = stringResource(R.string.analytics_empty_subtitle),
                modifier = Modifier.fillMaxWidth(),
            )
            else -> AnalyticsContent(d)
        }
    }
}

@Composable
private fun AnalyticsContent(d: AnalyticsData) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.screen),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        // --- Headline stats ---
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                label = stringResource(R.string.analytics_honey_this_year),
                value = stringResource(R.string.analytics_kg, formatKg(d.honeyThisYearKg)),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.analytics_honey_all_time),
                value = stringResource(R.string.analytics_kg, formatKg(d.honeyAllTimeKg)),
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                label = stringResource(R.string.analytics_hives_tracked),
                value = d.hivesTracked.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.analytics_avg_mite),
                value = d.avgMiteLoad?.let { "${formatKg(it)}%" } ?: stringResource(R.string.pdf_none),
                modifier = Modifier.weight(1f),
            )
        }

        // --- Honey yield by year ---
        if (d.hasHoney) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                SectionHeader(stringResource(R.string.analytics_honey_title))
                BarChart(
                    bars = d.honeyByYear.map { BarDatum(it.year.toString(), it.kg) },
                    color = MaterialTheme.statusColors.dueSoon,
                )
            }
        }

        // --- Varroa load over time ---
        if (d.hasVarroa) {
            val caution = MaterialTheme.statusColors.dueSoon
            val action = MaterialTheme.statusColors.doNow
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                SectionHeader(stringResource(R.string.analytics_varroa_title))
                Text(
                    stringResource(R.string.analytics_varroa_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TrendScatter(
                    points = d.varroaPoints.map {
                        ScatterPoint(it.timeMs, it.perHundred, miteColor(it.result))
                    },
                    thresholds = listOf(
                        ThresholdLine(AnalyticsComputer.MITE_CAUTION_THRESHOLD, caution),
                        ThresholdLine(AnalyticsComputer.MITE_ACTION_THRESHOLD, action),
                    ),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.padding(top = Spacing.xs),
                ) {
                    LegendDot(caution, stringResource(R.string.analytics_legend_caution))
                    LegendDot(action, stringResource(R.string.analytics_legend_action))
                }
                Text(
                    UiFormat.fullDate(d.varroaPoints.first().timeMs) + " – " +
                        UiFormat.fullDate(d.varroaPoints.last().timeMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // --- Inspection cadence ---
        if (d.hasInspections) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                SectionHeader(stringResource(R.string.analytics_cadence_title))
                Text(
                    stringResource(R.string.analytics_cadence_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BarChart(
                    bars = d.inspectionsByMonth.map {
                        BarDatum(UiFormat.monthName(it.yearMonth.month).take(1), it.count.toDouble())
                    },
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(Spacing.md)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        androidx.compose.foundation.layout.Box(
            Modifier.size(10.dp).clip(CircleShape).background(color),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun miteColor(result: MiteResult?): Color = when (result) {
    MiteResult.LOW -> MaterialTheme.statusColors.healthy
    MiteResult.MODERATE -> MaterialTheme.statusColors.watchlist
    MiteResult.HIGH -> MaterialTheme.statusColors.dueSoon
    MiteResult.CRITICAL -> MaterialTheme.statusColors.doNow
    null -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun formatKg(v: Double): String = "%.1f".format(v)
