package io.github.max_schall.appiary.ui.screen.apiaries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PriorityHigh
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.dao.ApiaryStats
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.components.EmptyState
import io.github.max_schall.appiary.ui.components.NameInputDialog
import io.github.max_schall.appiary.ui.components.StatusBadge
import io.github.max_schall.appiary.ui.theme.Spacing
import io.github.max_schall.appiary.ui.theme.statusColors
import io.github.max_schall.appiary.ui.util.UiFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiariesScreen(
    onOpenApiary: (String) -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApiariesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val apiaries by viewModel.apiaries.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.nav_apiaries)) },
            actions = {
                IconButton(onClick = onOpenMap) {
                    Icon(Icons.Outlined.Map, contentDescription = stringResource(R.string.map_title))
                }
                IconButton(onClick = { adding = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_apiary))
                }
            },
        )
        if (apiaries.isEmpty()) {
            EmptyState(
                Icons.Outlined.Grass,
                stringResource(R.string.apiaries_empty_title),
                stringResource(R.string.apiaries_empty_subtitle),
            )
        } else {
        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(apiaries, key = { it.id }) { a ->
                ApiaryCard(a, onClick = { onOpenApiary(a.id) })
            }
        }
        }
    }

    if (adding) {
        NameInputDialog(
            title = stringResource(R.string.new_apiary),
            label = stringResource(R.string.apiary_name),
            confirmLabel = stringResource(R.string.action_add),
            onDismiss = { adding = false },
            onConfirm = { viewModel.addApiary(it); adding = false },
        )
    }
}

/**
 * Capture an apiary's coordinates: prefilled from the device's last-known fix
 * when available, otherwise typed in. Confirms only with two valid numbers.
 */
@Composable
fun SetLocationDialog(
    initial: Pair<Double, Double>?,
    onDismiss: () -> Unit,
    onConfirm: (latitude: Double, longitude: Double) -> Unit,
) {
    var lat by remember { mutableStateOf(initial?.first?.toString() ?: "") }
    var lng by remember { mutableStateOf(initial?.second?.toString() ?: "") }
    val latValue = lat.trim().toDoubleOrNull()
    val lngValue = lng.trim().toDoubleOrNull()
    val valid = latValue != null && latValue in -90.0..90.0 && lngValue != null && lngValue in -180.0..180.0

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.map_set_location)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    stringResource(R.string.map_set_location_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = lat, onValueChange = { lat = it },
                        label = { Text(stringResource(R.string.season_lat)) },
                        singleLine = true, modifier = Modifier.weight(1f),
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = lng, onValueChange = { lng = it },
                        label = { Text(stringResource(R.string.season_lon)) },
                        singleLine = true, modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onConfirm(latValue!!, lngValue!!) },
                enabled = valid,
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun ApiaryCard(a: ApiaryStats, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(a.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    androidx.compose.ui.res.pluralStringResource(R.plurals.hive_count, a.hiveCount, a.hiveCount) +
                        " · " + stringResource(R.string.last_visit, UiFormat.relativeOrNever(a.lastVisitAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
                if (a.openRecommendationCount > 0) {
                    val s = MaterialTheme.statusColors
                    StatusBadge(
                        label = androidx.compose.ui.res.pluralStringResource(
                            R.plurals.open_items, a.openRecommendationCount, a.openRecommendationCount,
                        ),
                        icon = Icons.Outlined.PriorityHigh,
                        container = s.dueSoon,
                        onContainer = s.onDueSoon,
                        modifier = Modifier.padding(top = 6.dp),
                    )
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
