package io.github.max_schall.appiary.ui.screen.apiaries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ReceiptLong
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
import io.github.max_schall.appiary.ui.components.HiveListItem
import io.github.max_schall.appiary.ui.components.NameInputDialog
import io.github.max_schall.appiary.ui.components.SectionHeader
import io.github.max_schall.appiary.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiaryDetailScreen(
    onBack: () -> Unit,
    onOpenHive: (String) -> Unit,
    onOpenRecordBook: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApiaryDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var addingHive by remember { mutableStateOf(false) }
    var capturingSwarm by remember { mutableStateOf(false) }
    var settingLocation by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var explaining by remember { mutableStateOf<io.github.max_schall.appiary.ui.model.RecommendationItem?>(null) }

    fun actOn(item: io.github.max_schall.appiary.ui.model.RecommendationItem) {
        if (item.rec.category == io.github.max_schall.appiary.domain.model.RecommendationCategory.COMPLIANCE) {
            onOpenRecordBook(viewModel.apiaryId)
        } else {
            item.rec.hiveId?.let(onOpenHive)
        }
    }

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(state.apiaryName) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            },
            actions = {
                IconButton(onClick = { onOpenRecordBook(viewModel.apiaryId) }) {
                    Icon(
                        Icons.Outlined.ReceiptLong,
                        contentDescription = stringResource(R.string.recordbook_open),
                    )
                }
                IconButton(onClick = { addingHive = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_hive))
                }
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more))
                }
                androidx.compose.material3.DropdownMenu(menuOpen, onDismissRequest = { menuOpen = false }) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(stringResource(R.string.colony_capture_action)) },
                        onClick = { menuOpen = false; capturingSwarm = true },
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(stringResource(R.string.map_set_location)) },
                        onClick = { menuOpen = false; settingLocation = true },
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
            item {
                Text(
                    androidx.compose.ui.res.pluralStringResource(R.plurals.hive_count, state.hives.size, state.hives.size) +
                        " · " + androidx.compose.ui.res.pluralStringResource(R.plurals.open_items, state.openItems.size, state.openItems.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.hives.isNotEmpty()) {
                item {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { state.hives.firstOrNull()?.let { onOpenHive(it.hive.id) } },
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.PlaylistAddCheck, contentDescription = null)
                        Text(
                            stringResource(R.string.start_inspection_round),
                            modifier = Modifier.padding(start = Spacing.sm),
                        )
                    }
                }
            }

            if (state.openItems.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.section_needs_attention)) }
                items(state.openItems, key = { it.key }) { group ->
                    io.github.max_schall.appiary.ui.components.GroupedRecommendationCard(
                        group = group,
                        onPrimaryAction = ::actOn,
                        onExplain = { explaining = it },
                        onComplete = { viewModel.complete(it.rec) },
                        onSnooze = { viewModel.snooze(it.rec) },
                        onDismiss = { viewModel.dismiss(it.rec) },
                    )
                }
            }

            item { SectionHeader(stringResource(R.string.section_hives)) }
            items(state.hives, key = { it.hive.id }) { ui ->
                HiveListItem(ui, onClick = { onOpenHive(ui.hive.id) })
            }
        }
    }

    if (addingHive) {
        NameInputDialog(
            title = stringResource(R.string.new_hive),
            label = stringResource(R.string.hive_name_label),
            confirmLabel = stringResource(R.string.action_add),
            onDismiss = { addingHive = false },
            onConfirm = { viewModel.addHive(it); addingHive = false },
        )
    }

    if (capturingSwarm) {
        NameInputDialog(
            title = stringResource(R.string.colony_capture_title),
            label = stringResource(R.string.colony_capture_name),
            confirmLabel = stringResource(R.string.colony_capture_action),
            onDismiss = { capturingSwarm = false },
            onConfirm = { viewModel.captureSwarm(it); capturingSwarm = false },
        )
    }

    if (settingLocation) {
        SetLocationDialog(
            initial = viewModel.currentLocation(),
            onDismiss = { settingLocation = false },
            onConfirm = { lat, lng ->
                viewModel.setLocation(lat, lng)
                settingLocation = false
            },
        )
    }

    explaining?.let { item ->
        io.github.max_schall.appiary.ui.components.ExplanationSheet(
            item = item,
            onDismiss = { explaining = null },
            onPrimaryAction = { explaining = null; actOn(item) },
        )
    }
}
