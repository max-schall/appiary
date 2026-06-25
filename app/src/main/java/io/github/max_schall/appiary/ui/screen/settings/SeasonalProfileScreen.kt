package io.github.max_schall.appiary.ui.screen.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.theme.Spacing
import io.github.max_schall.appiary.ui.util.UiFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonalProfileScreen(
    onBack: () -> Unit,
    viewModel: SeasonalProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val climate by viewModel.climate.collectAsStateWithLifecycle()
    val season by viewModel.seasonModel.collectAsStateWithLifecycle()
    LaunchedEffect(saved) { if (saved) onBack() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.useDeviceLocation() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.season_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth().padding(Spacing.screen),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Text(stringResource(R.string.action_save), modifier = Modifier.padding(start = Spacing.sm))
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            OutlinedTextField(
                value = form.name,
                onValueChange = { v -> viewModel.update { it.copy(name = v) } },
                label = { Text(stringResource(R.string.season_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // --- Location-based derivation ---
            Text(stringResource(R.string.season_location_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CoordField(stringResource(R.string.season_lat), form.latitude, Modifier.weight(1f)) { v -> viewModel.update { it.copy(latitude = v) } }
                CoordField(stringResource(R.string.season_lon), form.longitude, Modifier.weight(1f)) { v -> viewModel.update { it.copy(longitude = v) } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                        ),
                    )
                }) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null)
                    Text(stringResource(R.string.season_use_location), modifier = Modifier.padding(start = Spacing.sm))
                }
                Button(onClick = viewModel::deriveFromLocation, enabled = !busy) {
                    Text(stringResource(R.string.season_derive))
                }
            }
            status?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            climate?.let { c ->
                Text(
                    stringResource(R.string.season_climate_header),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.season_climate_line, c.code, c.hardinessZone, stringResource(c.winterSeverity.labelRes())),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            season?.let { sm ->
                Text(
                    stringResource(R.string.season_now_header),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.season_now_phase, stringResource(sm.phase.labelRes())),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(R.string.season_now_flow, stringResource(sm.flow.labelRes())),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // --- Manual fields ---
            HemisphereChoice(form.hemisphere) { v -> viewModel.update { it.copy(hemisphere = v) } }
            MonthStepper(stringResource(R.string.season_active_start), form.activeStart) { v -> viewModel.update { it.copy(activeStart = v) } }
            MonthStepper(stringResource(R.string.season_active_end), form.activeEnd) { v -> viewModel.update { it.copy(activeEnd = v) } }
            MonthStepper(stringResource(R.string.season_harvest_start), form.harvestStart) { v -> viewModel.update { it.copy(harvestStart = v) } }
            MonthStepper(stringResource(R.string.season_harvest_end), form.harvestEnd) { v -> viewModel.update { it.copy(harvestEnd = v) } }
            MonthStepper(stringResource(R.string.season_winter_prep), form.winterPrep) { v -> viewModel.update { it.copy(winterPrep = v) } }
        }
    }
}

@Composable
private fun CoordField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter { it.isDigit() || it == '.' || it == '-' }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HemisphereChoice(selected: Hemisphere, onSelect: (Hemisphere) -> Unit) {
    Column {
        Text(stringResource(R.string.season_hemisphere), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            val options = Hemisphere.entries
            options.forEachIndexed { i, h ->
                SegmentedButton(
                    selected = h == selected,
                    onClick = { onSelect(h) },
                    shape = SegmentedButtonDefaults.itemShape(i, options.size),
                ) {
                    Text(stringResource(if (h == Hemisphere.NORTHERN) R.string.hemi_northern else R.string.hemi_southern))
                }
            }
        }
    }
}

@Composable
private fun MonthStepper(label: String, month: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        IconButton(onClick = { onChange((month + 10) % 12 + 1) }) {
            Icon(Icons.Filled.Remove, contentDescription = null)
        }
        Text(UiFormat.monthName(month), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        IconButton(onClick = { onChange(month % 12 + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}
