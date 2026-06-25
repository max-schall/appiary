package io.github.max_schall.appiary.ui.screen.recordbook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.entity.MedicineReceiptEntity
import io.github.max_schall.appiary.ui.screen.log.DateField
import io.github.max_schall.appiary.ui.theme.Spacing
import io.github.max_schall.appiary.ui.util.UiFormat

/**
 * Bottom sheet to complete a treatment's record-book entry: the medicine name and
 * withdrawal period (both legally required, withdrawal even when zero), plus proof
 * of purchase — photograph a new receipt, link an existing one, or unlink.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkReceiptSheet(
    treatment: io.github.max_schall.appiary.data.entity.TreatmentEventEntity,
    existing: List<ReceiptRow>,
    hasReceipt: Boolean,
    onSaveDetails: (productName: String?, withdrawalDays: Int?) -> Unit,
    onTakePhoto: () -> Unit,
    onPick: (String) -> Unit,
    onUnlink: () -> Unit,
    onDismiss: () -> Unit,
) {
    var product by rememberSaveable(treatment.id) { mutableStateOf(treatment.productName.orEmpty()) }
    var withdrawal by rememberSaveable(treatment.id) {
        mutableStateOf(treatment.withdrawalDays?.toString() ?: "")
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.sm)) {
            Text(
                stringResource(R.string.recordbook_details_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
            OutlinedTextField(
                value = product, onValueChange = { product = it },
                label = { Text(stringResource(R.string.recordbook_product)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = withdrawal,
                onValueChange = { v -> withdrawal = v.filter { it.isDigit() }.take(4) },
                label = { Text(stringResource(R.string.recordbook_withdrawal_days)) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm),
            )
            TextButton(
                onClick = {
                    onSaveDetails(product.trim().ifBlank { null }, withdrawal.toIntOrNull())
                },
                modifier = Modifier.align(Alignment.End),
            ) { Text(stringResource(R.string.action_save)) }

            HorizontalDivider(Modifier.padding(vertical = Spacing.sm))

            Text(
                stringResource(R.string.recordbook_link_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )

            SheetAction(Icons.Outlined.AddAPhoto, stringResource(R.string.recordbook_take_photo), onTakePhoto)

            if (hasReceipt) {
                SheetAction(Icons.Outlined.LinkOff, stringResource(R.string.recordbook_unlink), onUnlink)
            }

            if (existing.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
                Text(
                    stringResource(R.string.recordbook_choose_existing),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.xs),
                )
                existing.forEach { rr ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(rr.receipt.id) }
                            .padding(vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp))
                        Column {
                            Text(
                                rr.receipt.supplierName?.takeIf { it.isNotBlank() }
                                    ?: stringResource(R.string.receipt_no_supplier),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            val sub = buildList {
                                rr.receipt.purchaseDate?.let { add(UiFormat.fullDate(it)) }
                                add(pluralStringResource(R.plurals.receipt_linked_count, rr.linkedCount, rr.linkedCount))
                            }.joinToString(" · ")
                            Text(
                                sub,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.lg))
        }
    }
}

@Composable
private fun SheetAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(icon, contentDescription = null)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Editor for a receipt's supplier/vet details (the photo is captured separately). */
@Composable
fun ReceiptEditorDialog(
    receipt: MedicineReceiptEntity,
    onSave: (MedicineReceiptEntity) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var supplierName by rememberSaveable(receipt.id) { mutableStateOf(receipt.supplierName.orEmpty()) }
    var supplierAddress by rememberSaveable(receipt.id) { mutableStateOf(receipt.supplierAddress.orEmpty()) }
    var label by rememberSaveable(receipt.id) { mutableStateOf(receipt.label.orEmpty()) }
    var vetName by rememberSaveable(receipt.id) { mutableStateOf(receipt.vetName.orEmpty()) }
    var vetContact by rememberSaveable(receipt.id) { mutableStateOf(receipt.vetContact.orEmpty()) }
    var purchaseDate by rememberSaveable(receipt.id) {
        mutableStateOf(receipt.purchaseDate ?: System.currentTimeMillis())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.receipt_edit_title)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                OutlinedTextField(
                    value = supplierName, onValueChange = { supplierName = it },
                    label = { Text(stringResource(R.string.receipt_supplier_name)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = supplierAddress, onValueChange = { supplierAddress = it },
                    label = { Text(stringResource(R.string.receipt_supplier_address)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                DateField(
                    label = stringResource(R.string.receipt_purchase_date),
                    value = purchaseDate,
                    onChange = { purchaseDate = it },
                )
                OutlinedTextField(
                    value = label, onValueChange = { label = it },
                    label = { Text(stringResource(R.string.receipt_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = vetName, onValueChange = { vetName = it },
                    label = { Text(stringResource(R.string.receipt_vet_name)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = vetContact, onValueChange = { vetContact = it },
                    label = { Text(stringResource(R.string.receipt_vet_contact)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.Start),
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.receipt_delete), modifier = Modifier.padding(start = Spacing.xs))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    receipt.copy(
                        supplierName = supplierName.trim().ifBlank { null },
                        supplierAddress = supplierAddress.trim().ifBlank { null },
                        purchaseDate = purchaseDate,
                        label = label.trim().ifBlank { null },
                        vetName = vetName.trim().ifBlank { null },
                        vetContact = vetContact.trim().ifBlank { null },
                    ),
                )
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
