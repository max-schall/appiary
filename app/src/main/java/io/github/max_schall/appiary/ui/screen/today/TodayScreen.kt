package io.github.max_schall.appiary.ui.screen.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.components.ApiaryFilterRowInline
import io.github.max_schall.appiary.ui.components.EmptyState
import io.github.max_schall.appiary.ui.components.ExplanationSheet
import io.github.max_schall.appiary.ui.components.GroupedRecommendationCard
import io.github.max_schall.appiary.ui.components.SummaryCounters
import io.github.max_schall.appiary.ui.model.RecommendationItem
import io.github.max_schall.appiary.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onOpenHive: (String) -> Unit,
    onOpenRecordBook: (String) -> Unit,
    onOpenInsights: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var explaining by remember { mutableStateOf<RecommendationItem?>(null) }

    fun primaryAction(item: RecommendationItem) {
        if (item.rec.category == io.github.max_schall.appiary.domain.model.RecommendationCategory.COMPLIANCE) {
            item.rec.apiaryId?.let(onOpenRecordBook)
        } else {
            item.rec.hiveId?.let(onOpenHive)
        }
    }

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.nav_today)) },
            actions = {
                IconButton(onClick = onOpenInsights) {
                    Icon(
                        Icons.Outlined.Insights,
                        contentDescription = stringResource(R.string.analytics_title),
                    )
                }
            },
        )

        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 96.dp,
            ),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                SummaryCounters(
                    counts = state.counts,
                    selected = state.selectedBucket,
                    onSelect = viewModel::toggleBucketFilter,
                )
            }
            if (state.apiaries.size >= 2) {
                item {
                    ApiaryFilterRowInline(
                        apiaries = state.apiaries,
                        selectedId = state.selectedApiaryId,
                        onSelect = viewModel::setApiaryFilter,
                    )
                }
            }

            if (state.groups.isEmpty() && !state.loading) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.CheckCircle,
                        title = stringResource(R.string.today_empty_title),
                        subtitle = stringResource(R.string.today_empty_subtitle),
                    )
                }
            }

            items(state.groups, key = { it.key }) { group ->
                GroupedRecommendationCard(
                    group = group,
                    onPrimaryAction = { primaryAction(it) },
                    onExplain = { explaining = it },
                    onComplete = { viewModel.complete(it.rec) },
                    onSnooze = { viewModel.snooze(it.rec) },
                    onDismiss = { viewModel.dismiss(it.rec) },
                )
            }
        }
    }

    explaining?.let { item ->
        ExplanationSheet(
            item = item,
            onDismiss = { explaining = null },
            onPrimaryAction = { explaining = null; primaryAction(item) },
        )
    }
}
