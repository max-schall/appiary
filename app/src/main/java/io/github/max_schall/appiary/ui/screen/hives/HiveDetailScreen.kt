package io.github.max_schall.appiary.ui.screen.hives

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Hive
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.navigation.QuickAddAction
import io.github.max_schall.appiary.ui.components.AttributeChip
import io.github.max_schall.appiary.ui.components.ExplanationSheet
import io.github.max_schall.appiary.ui.components.HiveStatusDot
import io.github.max_schall.appiary.ui.components.RecommendationCard
import io.github.max_schall.appiary.ui.components.SectionHeader
import io.github.max_schall.appiary.ui.model.RecommendationItem
import io.github.max_schall.appiary.ui.model.TimelineEntry
import io.github.max_schall.appiary.ui.model.TimelineKind
import io.github.max_schall.appiary.ui.theme.Spacing
import io.github.max_schall.appiary.ui.util.UiFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HiveDetailScreen(
    onBack: () -> Unit,
    onLogAction: (QuickAddAction, String) -> Unit,
    onAddPhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HiveDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mergeCandidates by viewModel.mergeCandidates.collectAsStateWithLifecycle()
    var explaining by remember { mutableStateOf<RecommendationItem?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var showSplit by remember { mutableStateOf(false) }
    var showMerge by remember { mutableStateOf(false) }
    var showWeigh by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val hive = state.hive
    val tagLinkedMsg = stringResource(R.string.nfc_tag_linked)
    val scanFirstMsg = stringResource(R.string.nfc_scan_first)

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(hive?.name ?: stringResource(R.string.field_hive)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            },
            actions = {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more))
                }
                androidx.compose.material3.DropdownMenu(menuOpen, onDismissRequest = { menuOpen = false }) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(stringResource(R.string.weigh_title)) },
                        onClick = { menuOpen = false; showWeigh = true },
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(stringResource(R.string.colony_split_title)) },
                        onClick = { menuOpen = false; showSplit = true },
                    )
                    if (mergeCandidates.isNotEmpty()) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(stringResource(R.string.colony_merge_title)) },
                            onClick = { menuOpen = false; showMerge = true },
                        )
                    }
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(stringResource(R.string.link_nfc_tag)) },
                        onClick = {
                            menuOpen = false
                            viewModel.linkNfcTag { ok ->
                                android.widget.Toast.makeText(
                                    context,
                                    if (ok) tagLinkedMsg else scanFirstMsg,
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                    )
                }
            },
        )

        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (hive != null) {
                item { HiveHeader(hive) }
                item { SectionHeader(stringResource(R.string.section_current_state)) }
                item { SnapshotChips(hive) }
                item { SectionHeader(stringResource(R.string.section_log)) }
                item { QuickActionsRow(onAction = { onLogAction(it, viewModel.hiveId) }) }
                item { SectionHeader(stringResource(R.string.section_photos)) }
                item { PhotosRow(state.photos, onAddPhoto = { onAddPhoto(viewModel.hiveId) }) }
            }

            if (state.nextActions.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.section_next_actions)) }
                items(state.nextActions, key = { "rec-${it.rec.id}" }) { item ->
                    RecommendationCard(
                        item = item,
                        onPrimaryAction = { explaining = item },
                        onExplain = { explaining = item },
                        onComplete = { viewModel.complete(item.rec.id) },
                        onSnooze = { viewModel.snooze(item.rec.id) },
                        onDismiss = { viewModel.dismiss(item.rec.id) },
                    )
                }
            }

            item { SectionHeader(stringResource(R.string.section_history)) }
            if (state.timeline.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.timeline, key = { it.id }) { entry -> TimelineRow(entry) }
        }
    }

    explaining?.let { item ->
        ExplanationSheet(item = item, onDismiss = { explaining = null }, onPrimaryAction = { explaining = null })
    }

    if (showSplit) {
        SplitColonyDialog(
            onDismiss = { showSplit = false },
            onConfirm = { name, keepsQueen ->
                showSplit = false
                viewModel.splitColony(name, keepsQueen)
            },
        )
    }
    if (showMerge) {
        MergeColonyDialog(
            candidates = mergeCandidates,
            onDismiss = { showMerge = false },
            onConfirm = { targetId ->
                showMerge = false
                viewModel.mergeInto(targetId)
                onBack()
            },
        )
    }
    if (showWeigh) {
        WeighDialog(
            onDismiss = { showWeigh = false },
            onConfirm = { kg ->
                showWeigh = false
                viewModel.logWeight(kg)
            },
        )
    }
}

@Composable
private fun PhotosRow(
    photos: List<io.github.max_schall.appiary.data.entity.PhotoAttachmentEntity>,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        androidx.compose.material3.AssistChip(
            onClick = onAddPhoto,
            label = { Text(stringResource(R.string.add_photo)) },
            leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
        )
        photos.forEach { photo ->
            coil.compose.AsyncImage(
                model = photo.uri,
                contentDescription = photo.caption ?: stringResource(R.string.section_photos),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionsRow(onAction: (QuickAddAction) -> Unit, modifier: Modifier = Modifier) {
    val actions = listOf(
        QuickAddAction.Inspection, QuickAddAction.MiteCheck, QuickAddAction.Treatment,
        QuickAddAction.Feeding, QuickAddAction.Harvest,
    )
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        actions.forEach { action ->
            androidx.compose.material3.AssistChip(
                onClick = { onAction(action) },
                label = { Text(stringResource(action.labelRes())) },
            )
        }
    }
}

@Composable
private fun HiveHeader(hive: HiveEntity) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HiveStatusDot(hive.status)
        Column(Modifier.padding(start = Spacing.md)) {
            Text(stringResource(hive.status.labelRes()), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                stringResource(
                    R.string.hive_last_seen,
                    UiFormat.relativeOrNever(hive.lastInspectionAt),
                    UiFormat.relativeOrNever(hive.lastMiteCheckAt),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SnapshotChips(hive: HiveEntity) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        AttributeChip(stringResource(R.string.attr_queen), stringResource(hive.queenStatus.labelRes()))
        AttributeChip(stringResource(R.string.attr_brood), stringResource(hive.broodPattern.labelRes()))
        AttributeChip(stringResource(R.string.attr_strength), stringResource(hive.strength.labelRes()))
        AttributeChip(stringResource(R.string.attr_temperament), stringResource(hive.temperament.labelRes()))
        AttributeChip(stringResource(R.string.attr_stores), stringResource(hive.foodStores.labelRes()))
        AttributeChip(stringResource(R.string.attr_treatment), stringResource(hive.treatmentState.labelRes()))
        if (hive.originType != io.github.max_schall.appiary.domain.model.HiveOrigin.UNKNOWN) {
            AttributeChip(stringResource(R.string.attr_origin), stringResource(hive.originType.labelRes()))
        }
    }
}

private fun timelineIcon(kind: TimelineKind): ImageVector = when (kind) {
    TimelineKind.INSPECTION -> Icons.Outlined.Search
    TimelineKind.MITE -> Icons.Outlined.BugReport
    TimelineKind.TREATMENT -> Icons.Outlined.Medication
    TimelineKind.FEEDING -> Icons.Filled.WaterDrop
    TimelineKind.HARVEST -> Icons.Outlined.Inventory2
    TimelineKind.QUEEN -> Icons.Outlined.Star
    TimelineKind.COLONY -> Icons.Outlined.Hive
    TimelineKind.WEIGHT -> Icons.Outlined.Scale
}

@Composable
private fun TimelineRow(entry: TimelineEntry, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.small) {
                Icon(
                    timelineIcon(entry.kind),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp).size(18.dp),
                )
            }
            Column(Modifier.weight(1f).padding(start = Spacing.md)) {
                Text(entry.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(entry.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                UiFormat.shortDate(entry.timestamp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
