package io.github.max_schall.appiary.ui.screen.recordbook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.entity.MedicineReceiptEntity
import io.github.max_schall.appiary.data.entity.TreatmentEventEntity
import io.github.max_schall.appiary.data.export.BestandsbuchEntry
import io.github.max_schall.appiary.data.export.BestandsbuchExport
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.ReceiptRepository
import io.github.max_schall.appiary.data.repository.TreatmentRepository
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.util.UiFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream

/** One treatment row of the record book, with its linked receipt (if any). */
data class RecordRow(
    val treatment: TreatmentEventEntity,
    val hiveName: String?,
    val receipt: MedicineReceiptEntity?,
)

/** A receipt plus how many of this apiary's treatments reference it. */
data class ReceiptRow(
    val receipt: MedicineReceiptEntity,
    val linkedCount: Int,
)

data class RecordBookUiState(
    val apiaryName: String = "",
    val countryCode: String? = null,
    val isGermany: Boolean = false,
    val rows: List<RecordRow> = emptyList(),
    val receipts: List<ReceiptRow> = emptyList(),
)

/**
 * Backs the per-apiary German Bestandsbuch screen (EU 2019/6 Art. 108). Pulls
 * treatments straight from the log, joins them to proof-of-purchase receipts,
 * resolves the apiary's country for the legal gate, and assembles the PDF export.
 */
class RecordBookViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val apiaryRepo: ApiaryRepository,
    private val hiveRepo: HiveRepository,
    private val treatmentRepo: TreatmentRepository,
    private val receiptRepo: ReceiptRepository,
    private val refreshRecommendations: RefreshRecommendations,
) : AndroidViewModel(application) {

    val apiaryId: String = checkNotNull(savedStateHandle["apiaryId"])

    /** Country code of the apiary's site; resolved via reverse-geocoding on open. */
    private val countryCode = MutableStateFlow<String?>(null)

    /** Set after a fresh receipt is captured so the screen can open its editor. */
    private val _editReceiptId = MutableStateFlow<String?>(null)
    val editReceiptId = _editReceiptId.asStateFlow()

    init {
        viewModelScope.launch {
            val site = apiaryRepo.siteForApiary(apiaryId)
            countryCode.value = site?.countryCode
            // Auto-detect the country from coordinates when not yet known.
            if (site?.countryCode == null && site?.latitude != null && site.longitude != null) {
                val resolved = io.github.max_schall.appiary.util.CountryResolver.resolve(
                    getApplication(), site.latitude, site.longitude,
                )
                if (resolved != null) {
                    apiaryRepo.setLocation(apiaryId, site.latitude, site.longitude, resolved)
                    countryCode.value = resolved
                }
            }
        }
    }

    val uiState = combine(
        apiaryRepo.observeApiary(apiaryId),
        hiveRepo.observeByApiary(apiaryId),
        treatmentRepo.observeByApiary(apiaryId),
        receiptRepo.observeAll(),
        countryCode,
    ) { apiary, hives, treatments, receipts, country ->
        val receiptsById = receipts.associateBy { it.id }
        val hiveNames = hives.associate { it.id to it.name }
        val linkCounts = treatments.groupingBy { it.receiptId }.eachCount()
        RecordBookUiState(
            apiaryName = apiary?.name ?: "",
            countryCode = country,
            isGermany = country == "DE",
            rows = treatments.map {
                RecordRow(it, hiveNames[it.hiveId], it.receiptId?.let(receiptsById::get))
            },
            receipts = receipts.map { ReceiptRow(it, linkCounts[it.id] ?: 0) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordBookUiState())

    /** Capture the device's location for this apiary and resolve its country. [onResult] = success. */
    fun useDeviceLocation(onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val ll = io.github.max_schall.appiary.util.LocationProvider.lastKnown(getApplication())
        if (ll == null) { onResult(false); return@launch }
        val country = io.github.max_schall.appiary.util.CountryResolver.resolve(getApplication(), ll.first, ll.second)
        apiaryRepo.setLocation(apiaryId, ll.first, ll.second, country)
        countryCode.value = country
        refreshRecommendations()
        onResult(true)
    }

    fun newReceiptPhotoFile(): File = receiptRepo.newPhotoFile()

    /** Create a receipt from a captured photo, optionally linking a treatment, then open its editor. */
    fun onReceiptCaptured(file: File, linkTreatmentId: String?) = viewModelScope.launch {
        val id = receiptRepo.createFromPhoto(file)
        if (linkTreatmentId != null) treatmentRepo.linkReceipt(linkTreatmentId, id)
        refreshRecommendations()
        _editReceiptId.value = id
    }

    fun requestEdit(receiptId: String) { _editReceiptId.value = receiptId }
    fun clearEdit() { _editReceiptId.value = null }

    fun updateRecordDetails(treatmentId: String, productName: String?, withdrawalDays: Int?) =
        viewModelScope.launch { treatmentRepo.updateRecordDetails(treatmentId, productName, withdrawalDays) }

    fun linkReceipt(treatmentId: String, receiptId: String?) = viewModelScope.launch {
        treatmentRepo.linkReceipt(treatmentId, receiptId)
        refreshRecommendations()
    }

    fun saveReceipt(receipt: MedicineReceiptEntity) = viewModelScope.launch {
        receiptRepo.save(receipt)
    }

    fun deleteReceipt(receipt: MedicineReceiptEntity) = viewModelScope.launch {
        // Clear the link from any treatments at this apiary that referenced it.
        treatmentRepo.getByApiary(apiaryId)
            .filter { it.receiptId == receipt.id }
            .forEach { treatmentRepo.linkReceipt(it.id, null) }
        receiptRepo.delete(receipt)
        refreshRecommendations()
    }

    /** Assemble the localized export payload, then render it to [out] (e.g. a SAF stream). */
    suspend fun writePdf(out: OutputStream): Boolean {
        val state = uiState.value
        if (state.rows.isEmpty()) return false
        val ctx = getApplication<Application>()
        val none = ctx.getString(R.string.pdf_none)

        val entries = state.rows.map { row ->
            val t = row.treatment
            val r = row.receipt
            val medicine = t.productName?.takeIf { it.isNotBlank() }
                ?: ctx.getString(t.type.labelRes())
            val supplier = listOfNotNull(
                r?.supplierName?.takeIf { it.isNotBlank() },
                r?.supplierAddress?.takeIf { it.isNotBlank() },
            ).joinToString(", ").ifBlank { none }
            val duration = if (t.endedAt != null && t.endedAt != t.startedAt) {
                "${UiFormat.shortDate(t.startedAt)} – ${UiFormat.shortDate(t.endedAt)}"
            } else {
                UiFormat.shortDate(t.startedAt)
            }
            val vet = listOfNotNull(
                r?.vetName?.takeIf { it.isNotBlank() },
                r?.vetContact?.takeIf { it.isNotBlank() },
            ).joinToString(", ").ifBlank { none }
            BestandsbuchEntry(
                date = UiFormat.fullDate(t.startedAt),
                medicine = medicine,
                supplier = supplier,
                quantity = t.dosage?.takeIf { it.isNotBlank() } ?: none,
                duration = duration,
                // Withdrawal must be noted even when zero (Art. 108).
                withdrawal = ctx.getString(R.string.pdf_days, t.withdrawalDays ?: 0),
                identity = listOfNotNull(row.hiveName, state.apiaryName)
                    .joinToString(" · ").ifBlank { none },
                vet = vet,
            )
        }

        val photoUris = state.rows.mapNotNull { it.receipt }
            .distinctBy { it.id }
            .mapNotNull { it.photoUri }

        val export = BestandsbuchExport(
            title = ctx.getString(R.string.pdf_title),
            apiaryLine = ctx.getString(R.string.pdf_apiary, state.apiaryName),
            generatedLine = ctx.getString(R.string.pdf_generated, UiFormat.fullDate(System.currentTimeMillis())),
            columnHeaders = listOf(
                ctx.getString(R.string.pdf_col_date),
                ctx.getString(R.string.pdf_col_medicine),
                ctx.getString(R.string.pdf_col_supplier),
                ctx.getString(R.string.pdf_col_quantity),
                ctx.getString(R.string.pdf_col_duration),
                ctx.getString(R.string.pdf_col_withdrawal),
                ctx.getString(R.string.pdf_col_identity),
                ctx.getString(R.string.pdf_col_vet),
            ),
            entries = entries,
            receiptCaption = ctx.getString(R.string.pdf_receipt_caption),
            receiptPhotoUris = photoUris,
        )
        io.github.max_schall.appiary.data.export.BestandsbuchPdf.write(out, export)
        return true
    }
}
