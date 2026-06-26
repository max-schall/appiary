package io.github.max_schall.appiary.ui.screen.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.entity.InventoryItemEntity
import io.github.max_schall.appiary.domain.model.InventoryCategory
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.components.EmptyState
import io.github.max_schall.appiary.ui.components.SectionHeader
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var adding by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.inventory_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            },
            actions = {
                IconButton(onClick = { adding = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.inventory_add))
                }
            },
        )

        if (state.groups.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Inventory2,
                title = stringResource(R.string.inventory_empty_title),
                subtitle = stringResource(R.string.inventory_empty_subtitle),
                modifier = Modifier.fillMaxWidth(),
            )
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (state.lowStockCount > 0) {
                item {
                    Text(
                        stringResource(R.string.inventory_low_summary, state.lowStockCount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            state.groups.forEach { (category, items) ->
                item(key = "header-${category.name}") {
                    SectionHeader(stringResource(category.labelRes()))
                }
                items(items, key = { it.id }) { item ->
                    InventoryRow(
                        item = item,
                        onEdit = { editing = item },
                        onIncrement = { viewModel.adjust(item, 1.0) },
                        onDecrement = { viewModel.adjust(item, -1.0) },
                    )
                }
            }
        }
    }

    if (adding) {
        InventoryEditDialog(
            existing = null,
            onDismiss = { adding = false },
            onSave = { name, cat, qty, unit, threshold, notes ->
                viewModel.add(name, cat, qty, unit, threshold, notes); adding = false
            },
            onDelete = null,
        )
    }
    editing?.let { item ->
        InventoryEditDialog(
            existing = item,
            onDismiss = { editing = null },
            onSave = { name, cat, qty, unit, threshold, notes ->
                viewModel.save(
                    item.copy(
                        name = name, category = cat, quantity = qty,
                        unit = unit, lowStockThreshold = threshold, notes = notes,
                    ),
                )
                editing = null
            },
            onDelete = { viewModel.delete(item); editing = null },
        )
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItemEntity,
    onEdit: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = onEdit,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val qty = stringResource(R.string.inventory_qty, formatQty(item.quantity), item.unit ?: "")
                Text(
                    qty.trim(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.isLow) {
                    AssistChip(
                        onClick = onEdit,
                        label = { Text(stringResource(R.string.inventory_low_chip), fontWeight = FontWeight.SemiBold) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.padding(top = Spacing.xs),
                    )
                }
            }
            FilledTonalIconButton(onClick = onDecrement) {
                Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.inventory_decrement))
            }
            Box(Modifier.padding(horizontal = Spacing.xs))
            FilledTonalIconButton(onClick = onIncrement) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.inventory_increment))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryEditDialog(
    existing: InventoryItemEntity?,
    onDismiss: () -> Unit,
    onSave: (String, InventoryCategory, Double, String?, Double?, String?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: InventoryCategory.HARDWARE) }
    var quantity by remember { mutableStateOf(existing?.quantity?.let(::formatQty) ?: "") }
    var unit by remember { mutableStateOf(existing?.unit ?: "") }
    var threshold by remember { mutableStateOf(existing?.lowStockThreshold?.let(::formatQty) ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var categoryOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (existing == null) R.string.inventory_add else R.string.inventory_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.inventory_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ExposedDropdownMenuBox(
                    expanded = categoryOpen,
                    onExpandedChange = { categoryOpen = it },
                ) {
                    OutlinedTextField(
                        value = stringResource(category.labelRes()),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.inventory_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryOpen) },
                        modifier = Modifier
                            .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = categoryOpen, onDismissRequest = { categoryOpen = false }) {
                        InventoryCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(stringResource(cat.labelRes())) },
                                onClick = { category = cat; categoryOpen = false },
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text(stringResource(R.string.inventory_quantity)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(stringResource(R.string.inventory_unit)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = { Text(stringResource(R.string.inventory_threshold)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.inventory_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        name.trim(),
                        category,
                        quantity.parseQty() ?: 0.0,
                        unit.trim().ifBlank { null },
                        threshold.parseQty(),
                        notes.trim().ifBlank { null },
                    )
                },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Accept both "," and "." decimal separators, since DE users type commas. */
private fun String.parseQty(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Drop a trailing ".0" so whole counts read as "10" not "10.0". */
private fun formatQty(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
