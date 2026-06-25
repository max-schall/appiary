package io.github.max_schall.appiary.ui.screen.hives

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.ui.theme.Spacing

/** Split this colony off into a new daughter hive. */
@Composable
fun SplitColonyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, daughterKeepsQueen: Boolean) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var daughterKeepsQueen by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.colony_split_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.colony_daughter_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.colony_daughter_keeps_queen),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = daughterKeepsQueen, onCheckedChange = { daughterKeepsQueen = it })
                }
                Text(
                    stringResource(R.string.colony_split_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, daughterKeepsQueen) },
                enabled = name.isNotBlank(),
            ) { Text(stringResource(R.string.colony_split)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Quick scale reading for this hive (kg). */
@Composable
fun WeighDialog(
    onDismiss: () -> Unit,
    onConfirm: (weightKg: Double) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val value = text.trim().replace(',', '.').toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.weigh_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.weigh_label)) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { value?.let(onConfirm) },
                enabled = value != null && value > 0.0,
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Merge this colony into one of the other hives in the apiary. */
@Composable
fun MergeColonyDialog(
    candidates: List<HiveEntity>,
    onDismiss: () -> Unit,
    onConfirm: (targetId: String) -> Unit,
) {
    var selected by remember { mutableStateOf(candidates.firstOrNull()?.id) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.colony_merge_title)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.colony_merge_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                candidates.forEach { hive ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = selected == hive.id, onClick = { selected = hive.id }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected == hive.id, onClick = { selected = hive.id })
                        Text(hive.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selected?.let(onConfirm) },
                enabled = selected != null,
            ) { Text(stringResource(R.string.colony_merge)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
