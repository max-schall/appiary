package io.github.max_schall.appiary.ui.screen.hives

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
import io.github.max_schall.appiary.ui.i18n.labelRes
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

/** Log a queen event (requeen, mark, supersedure, loss, …) for this hive. */
@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)
@Composable
fun QueenEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        event: io.github.max_schall.appiary.domain.model.QueenEventType,
        status: io.github.max_schall.appiary.domain.model.QueenStatus,
        markColor: io.github.max_schall.appiary.domain.model.QueenMarkColor?,
        origin: String?,
        notes: String?,
    ) -> Unit,
) {
    var event by remember { mutableStateOf(io.github.max_schall.appiary.domain.model.QueenEventType.SEEN) }
    var status by remember { mutableStateOf(defaultStatusFor(event)) }
    var markColor by remember {
        mutableStateOf<io.github.max_schall.appiary.domain.model.QueenMarkColor?>(null)
    }
    var origin by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val suggested = remember { io.github.max_schall.appiary.domain.usecase.QueenMarking.colorForNow() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.queen_event_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                EnumDropdown(
                    label = stringResource(R.string.queen_event_type),
                    options = io.github.max_schall.appiary.domain.model.QueenEventType.entries,
                    selected = event,
                    optionLabel = { stringResource(it.labelRes()) },
                    onSelect = { event = it; status = defaultStatusFor(it) },
                )
                EnumDropdown(
                    label = stringResource(R.string.queen_event_status),
                    options = io.github.max_schall.appiary.domain.model.QueenStatus.entries,
                    selected = status,
                    optionLabel = { stringResource(it.labelRes()) },
                    onSelect = { status = it },
                )
                Text(
                    stringResource(R.string.queen_mark_color),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    io.github.max_schall.appiary.domain.model.QueenMarkColor.entries.forEach { color ->
                        androidx.compose.material3.FilterChip(
                            selected = markColor == color,
                            onClick = { markColor = if (markColor == color) null else color },
                            leadingIcon = {
                                androidx.compose.foundation.layout.Box(
                                    Modifier
                                        .size(16.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(androidx.compose.ui.graphics.Color(color.rgb))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            androidx.compose.foundation.shape.CircleShape,
                                        ),
                                )
                            },
                            label = { Text(stringResource(color.labelRes())) },
                        )
                    }
                }
                Text(
                    stringResource(R.string.queen_mark_suggested, stringResource(suggested.labelRes())),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text(stringResource(R.string.queen_origin)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.inventory_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(event, status, markColor, origin.ifBlank { null }, notes.ifBlank { null })
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Sensible default queen status implied by the event being logged. */
private fun defaultStatusFor(
    event: io.github.max_schall.appiary.domain.model.QueenEventType,
): io.github.max_schall.appiary.domain.model.QueenStatus = when (event) {
    io.github.max_schall.appiary.domain.model.QueenEventType.FAILED,
    io.github.max_schall.appiary.domain.model.QueenEventType.LOST ->
        io.github.max_schall.appiary.domain.model.QueenStatus.QUEENLESS
    else -> io.github.max_schall.appiary.domain.model.QueenStatus.QUEENRIGHT
}

/** Small reusable enum picker built on ExposedDropdownMenuBox. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    androidx.compose.material3.ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            modifier = Modifier
                .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = { onSelect(option); open = false },
                )
            }
        }
    }
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
