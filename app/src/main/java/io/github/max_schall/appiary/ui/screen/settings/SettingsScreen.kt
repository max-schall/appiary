package io.github.max_schall.appiary.ui.screen.settings

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.nfc.NfcController
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.util.AppLanguage
import io.github.max_schall.appiary.util.AppLocale
import io.github.max_schall.appiary.ui.components.SectionHeader
import io.github.max_schall.appiary.ui.components.ToggleRowSimple
import io.github.max_schall.appiary.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onEditSeasonalProfile: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val rules by viewModel.ruleConfig.collectAsStateWithLifecycle()
    val prefs by viewModel.appPrefs.collectAsStateWithLifecycle()
    val profileName by viewModel.seasonalProfileName.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nfcAvailable = (context as? Activity)?.let { NfcController.isAvailable(it) } ?: false
    var confirmRestore by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    val msgExported = stringResource(R.string.toast_backup_exported)
    val msgCsv = stringResource(R.string.toast_csv_exported)
    val msgRestored = stringResource(R.string.toast_backup_restored)
    val msgReadFailed = stringResource(R.string.toast_read_failed)
    val msgCleared = stringResource(R.string.toast_cleared)
    val msgRecomputed = stringResource(R.string.toast_recomputed)

    val exportJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            writeText(context, uri, viewModel.exportJson())
            toast(msgExported)
        }
    }
    val exportCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch { writeText(context, uri, viewModel.exportCsv()); toast(msgCsv) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val text = readText(context, uri)
            if (text != null) { viewModel.restore(text); toast(msgRestored) } else toast(msgReadFailed)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.nav_settings)) })
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen)
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            SectionHeader(stringResource(R.string.settings_language))
            LanguageSelector(onSelect = { lang -> AppLocale.set(lang); viewModel.recomputeNow() })

            HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
            SectionHeader(stringResource(R.string.settings_thresholds))
            val days = stringResource(R.string.unit_days)
            val oclock = stringResource(R.string.unit_oclock)
            Stepper(stringResource(R.string.settings_inspect_active), rules.activeSeasonInspectionIntervalDays, days, 3, 60) { v ->
                viewModel.updateRule { it.copy(activeSeasonInspectionIntervalDays = v) }
            }
            Stepper(stringResource(R.string.settings_inspect_off), rules.offSeasonInspectionIntervalDays, days, 7, 120) { v ->
                viewModel.updateRule { it.copy(offSeasonInspectionIntervalDays = v) }
            }
            Stepper(stringResource(R.string.settings_mite_interval), rules.miteCheckIntervalDays, days, 7, 90) { v ->
                viewModel.updateRule { it.copy(miteCheckIntervalDays = v) }
            }
            Stepper(stringResource(R.string.settings_queen_window), rules.queenFollowUpDays, days, 2, 30) { v ->
                viewModel.updateRule { it.copy(queenFollowUpDays = v) }
            }
            Stepper(stringResource(R.string.settings_swarm_window), rules.swarmFollowUpDays, days, 2, 21) { v ->
                viewModel.updateRule { it.copy(swarmFollowUpDays = v) }
            }
            Stepper(stringResource(R.string.settings_due_soon_window), rules.dueSoonWindowDays, days, 1, 14) { v ->
                viewModel.updateRule { it.copy(dueSoonWindowDays = v) }
            }
            ToggleRowSimple(stringResource(R.string.settings_watch_weak), rules.watchWeakColonies) { v ->
                viewModel.updateRule { it.copy(watchWeakColonies = v) }
            }
            OutlinedButton(onClick = viewModel::resetRules) { Text(stringResource(R.string.settings_reset_defaults)) }

            HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
            SectionHeader(stringResource(R.string.settings_weather))
            val celsius = stringResource(R.string.unit_celsius)
            Stepper(stringResource(R.string.settings_inspect_min_temp), rules.inspectionMinTempC.toInt(), celsius, 5, 25) { v ->
                viewModel.updateRule { it.copy(inspectionMinTempC = v.toDouble()) }
            }
            Stepper(stringResource(R.string.settings_treat_heat), rules.treatmentHeatMaxTempC.toInt(), celsius, 25, 40) { v ->
                viewModel.updateRule { it.copy(treatmentHeatMaxTempC = v.toDouble()) }
            }
            Stepper(stringResource(R.string.settings_cold_snap), rules.coldSnapMinTempC.toInt(), celsius, -10, 10) { v ->
                viewModel.updateRule { it.copy(coldSnapMinTempC = v.toDouble()) }
            }

            HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
            SectionHeader(stringResource(R.string.settings_reminders))
            ToggleRowSimple(stringResource(R.string.settings_daily_summary), prefs.dailySummaryEnabled) { v ->
                viewModel.updatePrefs { it.copy(dailySummaryEnabled = v) }
            }
            if (prefs.dailySummaryEnabled) {
                Stepper(stringResource(R.string.settings_summary_hour), prefs.summaryHour, oclock, 0, 23) { v ->
                    viewModel.updatePrefs { it.copy(summaryHour = v) }
                }
                Stepper(stringResource(R.string.settings_quiet_start), prefs.quietHoursStart, oclock, 0, 23) { v ->
                    viewModel.updatePrefs { it.copy(quietHoursStart = v) }
                }
                Stepper(stringResource(R.string.settings_quiet_end), prefs.quietHoursEnd, oclock, 0, 23) { v ->
                    viewModel.updatePrefs { it.copy(quietHoursEnd = v) }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
            SectionHeader(stringResource(R.string.settings_seasonal))
            Text(profileName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                stringResource(R.string.settings_seasonal_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onEditSeasonalProfile) { Text(stringResource(R.string.season_edit)) }

            HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
            SectionHeader(stringResource(R.string.settings_display_hardware))
            ToggleRowSimple(stringResource(R.string.settings_dynamic_color), prefs.dynamicColor) { v ->
                viewModel.updatePrefs { it.copy(dynamicColor = v) }
            }
            ToggleRowSimple(
                if (nfcAvailable) stringResource(R.string.settings_nfc_on) else stringResource(R.string.settings_nfc_unavailable),
                prefs.nfcEnabled && nfcAvailable,
            ) { v -> if (nfcAvailable) viewModel.updatePrefs { it.copy(nfcEnabled = v) } }

            HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
            SectionHeader(stringResource(R.string.settings_backup))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = { exportJsonLauncher.launch("appiary-backup.json") }) { Text(stringResource(R.string.settings_export_json)) }
                OutlinedButton(onClick = { exportCsvLauncher.launch("appiary-inspections.csv") }) { Text(stringResource(R.string.settings_export_csv)) }
            }
            OutlinedButton(onClick = { confirmRestore = true }) {
                Text(stringResource(R.string.settings_restore))
            }

            HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
            SectionHeader(stringResource(R.string.settings_debug))
            Button(onClick = { viewModel.recomputeNow(); toast(msgRecomputed) }) {
                Text(stringResource(R.string.settings_recompute))
            }
            OutlinedButton(onClick = { confirmClear = true }) { Text(stringResource(R.string.settings_clear_data)) }
        }
    }

    if (confirmRestore) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text(stringResource(R.string.restore_title)) },
            text = { Text(stringResource(R.string.restore_message)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    confirmRestore = false
                    importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                }) { Text(stringResource(R.string.restore_choose)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmRestore = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    if (confirmClear) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.clear_title)) },
            text = { Text(stringResource(R.string.clear_message)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    confirmClear = false; viewModel.clearAllData(); toast(msgCleared)
                }) { Text(stringResource(R.string.clear_confirm)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun LanguageSelector(onSelect: (AppLanguage) -> Unit, modifier: Modifier = Modifier) {
    val current = AppLocale.current()
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        AppLanguage.entries.forEach { lang ->
            val label = when (lang) {
                AppLanguage.SYSTEM -> stringResource(R.string.lang_system)
                AppLanguage.ENGLISH -> stringResource(R.string.lang_english)
                AppLanguage.GERMAN -> stringResource(R.string.lang_german)
            }
            androidx.compose.material3.FilterChip(
                selected = current == lang,
                onClick = { if (current != lang) onSelect(lang) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    value: Int,
    unit: String,
    min: Int,
    max: Int,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        IconButton(onClick = { if (value > min) onChange(value - 1) }) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
        }
        Text("$value$unit", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        IconButton(onClick = { if (value < max) onChange(value + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = "Increase")
        }
    }
}

private suspend fun writeText(context: android.content.Context, uri: Uri, text: String) {
    withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
    }
}

private suspend fun readText(context: android.content.Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
    }
