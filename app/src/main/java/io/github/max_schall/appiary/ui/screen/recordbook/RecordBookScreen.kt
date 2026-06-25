package io.github.max_schall.appiary.ui.screen.recordbook

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.components.SectionHeader
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.screen.photo.CameraCapture
import io.github.max_schall.appiary.ui.theme.Spacing
import io.github.max_schall.appiary.ui.util.UiFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordBookScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecordBookViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editReceiptId by viewModel.editReceiptId.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Which treatment is having a receipt attached (drives the link bottom sheet).
    var linkingTreatmentId by remember { mutableStateOf<String?>(null) }
    // When non-null, the full-screen camera is shown; value is the treatment to link on capture.
    var capturingForTreatment by remember { mutableStateOf<String?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    val exportDone = stringResource(R.string.recordbook_export_done)
    val exportEmpty = stringResource(R.string.recordbook_export_empty)
    val exportFailed = stringResource(R.string.recordbook_export_failed)
    val locationUnavailable = stringResource(R.string.recordbook_location_unavailable)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    fun tryLocation() = viewModel.useDeviceLocation { ok ->
        if (!ok) Toast.makeText(context, locationUnavailable, Toast.LENGTH_SHORT).show()
    }
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) tryLocation() else Toast.makeText(context, locationUnavailable, Toast.LENGTH_SHORT).show() }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { viewModel.writePdf(it) } ?: false
            }.getOrDefault(false)
            Toast.makeText(context, if (ok) exportDone else exportFailed, Toast.LENGTH_SHORT).show()
        }
    }

    if (showCamera) {
        Surface(Modifier.fillMaxSize()) {
            CameraCapture(
                onClose = { showCamera = false },
                newFile = viewModel::newReceiptPhotoFile,
                onCaptured = { file ->
                    showCamera = false
                    viewModel.onReceiptCaptured(file, capturingForTreatment)
                    capturingForTreatment = null
                },
            )
        }
        return
    }

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(state.apiaryName.ifBlank { stringResource(R.string.recordbook_title) })
                    Text(
                        stringResource(R.string.recordbook_subtitle_de),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (state.rows.isEmpty()) {
                            Toast.makeText(context, exportEmpty, Toast.LENGTH_SHORT).show()
                        } else {
                            pdfLauncher.launch("Bestandsbuch-${state.apiaryName.ifBlank { "Appiary" }}.pdf")
                        }
                    },
                ) {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = stringResource(R.string.recordbook_export))
                }
            },
        )

        LazyColumn(
            contentPadding = PaddingValues(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (state.countryCode == null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column(Modifier.padding(Spacing.md)) {
                            Text(
                                stringResource(R.string.recordbook_set_location_hint),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    if (io.github.max_schall.appiary.util.LocationProvider.hasPermission(context)) {
                                        tryLocation()
                                    } else {
                                        locationPermLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                            ) { Text(stringResource(R.string.recordbook_use_location)) }
                        }
                    }
                }
            } else if (!state.isGermany) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(
                            stringResource(R.string.recordbook_not_germany),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(Spacing.md),
                        )
                    }
                }
            }

            item { SectionHeader(stringResource(R.string.recordbook_section_treatments)) }
            if (state.rows.isEmpty()) {
                item { EmptyHint(stringResource(R.string.recordbook_no_treatments)) }
            } else {
                items(state.rows, key = { it.treatment.id }) { row ->
                    TreatmentRowCard(row, onClick = { linkingTreatmentId = row.treatment.id })
                }
            }

            item { SectionHeader(stringResource(R.string.recordbook_section_receipts)) }
            if (state.receipts.isEmpty()) {
                item { EmptyHint(stringResource(R.string.recordbook_no_receipts)) }
            } else {
                items(state.receipts, key = { it.receipt.id }) { rr ->
                    ReceiptRowCard(rr, onClick = { viewModel.requestEdit(rr.receipt.id) })
                }
            }
        }
    }

    // Bottom sheet: choose how to attach proof of purchase to a treatment.
    linkingTreatmentId?.let { treatmentId ->
        val row = state.rows.firstOrNull { it.treatment.id == treatmentId }
        if (row == null) { linkingTreatmentId = null; return@let }
        LinkReceiptSheet(
            treatment = row.treatment,
            existing = state.receipts,
            hasReceipt = row.receipt != null,
            onSaveDetails = { product, withdrawal ->
                viewModel.updateRecordDetails(treatmentId, product, withdrawal)
            },
            onTakePhoto = {
                linkingTreatmentId = null
                capturingForTreatment = treatmentId
                showCamera = true
            },
            onPick = { receiptId ->
                viewModel.linkReceipt(treatmentId, receiptId)
                linkingTreatmentId = null
            },
            onUnlink = {
                viewModel.linkReceipt(treatmentId, null)
                linkingTreatmentId = null
            },
            onDismiss = { linkingTreatmentId = null },
        )
    }

    // Receipt detail editor (opened after capture or by tapping a receipt).
    editReceiptId?.let { id ->
        val receipt = state.receipts.firstOrNull { it.receipt.id == id }?.receipt
        if (receipt != null) {
            ReceiptEditorDialog(
                receipt = receipt,
                onSave = { viewModel.saveReceipt(it); viewModel.clearEdit() },
                onDelete = { viewModel.deleteReceipt(receipt); viewModel.clearEdit() },
                onDismiss = { viewModel.clearEdit() },
            )
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = Spacing.sm),
    )
}

@Composable
private fun TreatmentRowCard(row: RecordRow, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(Spacing.md).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    row.treatment.productName?.takeIf { it.isNotBlank() }
                        ?: stringResource(row.treatment.type.labelRes()),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    UiFormat.fullDate(row.treatment.startedAt) +
                        (row.hiveName?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val linked = row.receipt != null
            AssistChip(
                onClick = onClick,
                leadingIcon = {
                    Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                label = {
                    Text(stringResource(if (linked) R.string.receipt_linked else R.string.receipt_missing))
                },
            )
        }
    }
}

@Composable
private fun ReceiptRowCard(rr: ReceiptRow, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.md).fillMaxWidth()) {
            Text(
                rr.receipt.supplierName?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.receipt_no_supplier),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            val parts = buildList {
                rr.receipt.purchaseDate?.let { add(UiFormat.fullDate(it)) }
                rr.receipt.label?.takeIf { it.isNotBlank() }?.let { add(it) }
            }
            if (parts.isNotEmpty()) {
                Text(
                    parts.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                pluralStringResource(R.plurals.receipt_linked_count, rr.linkedCount, rr.linkedCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
