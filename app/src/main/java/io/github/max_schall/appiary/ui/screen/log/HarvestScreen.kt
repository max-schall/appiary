package io.github.max_schall.appiary.ui.screen.log

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.data.entity.HarvestEventEntity
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HarvestRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.domain.model.HarvestProduct
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HarvestForm(
    val harvestedAt: Long,
    val product: HarvestProduct = HarvestProduct.HONEY,
    val amountKg: String = "",
    val batchNote: String = "",
    val notes: String = "",
)

class HarvestViewModel(
    savedStateHandle: SavedStateHandle,
    hiveRepo: HiveRepository,
    apiaryRepo: ApiaryRepository,
    private val harvestRepo: HarvestRepository,
    refreshRecommendations: RefreshRecommendations,
) : BaseLogViewModel(savedStateHandle, hiveRepo, apiaryRepo, refreshRecommendations) {

    private val _form = MutableStateFlow(HarvestForm(harvestedAt = clock()))
    val form = _form.asStateFlow()
    fun update(transform: (HarvestForm) -> HarvestForm) { _form.value = transform(_form.value) }

    fun save() = saveWith { hiveId, apiaryId ->
        val f = _form.value
        val now = clock()
        harvestRepo.save(
            HarvestEventEntity(
                id = newId(), hiveId = hiveId, apiaryId = apiaryId, harvestedAt = f.harvestedAt,
                product = f.product, amountKg = f.amountKg.toDoubleOrNull(),
                batchNote = f.batchNote.ifBlank { null }, notes = f.notes.ifBlank { null },
                createdAt = now, updatedAt = now,
            ),
        )
    }
}

@Composable
fun HarvestScreen(
    onDone: () -> Unit,
    viewModel: HarvestViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val hiveOptions by viewModel.hiveOptions.collectAsStateWithLifecycle()
    val selectedHiveId by viewModel.selectedHiveId.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    LaunchedEffect(saved) { if (saved) onDone() }

    LogScaffold(stringResource(R.string.log_harvest), onDone, viewModel::save, selectedHiveId != null) {
        if (selectedHiveId == null || hiveOptions.size > 1) {
            HivePickerField(hiveOptions, selectedHiveId, viewModel::selectHive)
        }
        DateField(stringResource(R.string.field_date), form.harvestedAt, { v -> viewModel.update { it.copy(harvestedAt = v) } })
        ChipChoice(stringResource(R.string.field_product), HarvestProduct.entries, form.product, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(product = v) }
        }
        DecimalField(stringResource(R.string.field_amount_kg), form.amountKg) { v -> viewModel.update { it.copy(amountKg = v) } }
        OutlinedTextField(
            value = form.batchNote,
            onValueChange = { v -> viewModel.update { it.copy(batchNote = v) } },
            label = { Text(stringResource(R.string.field_batch_note)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        NotesField(form.notes, { v -> viewModel.update { it.copy(notes = v) } })
    }
}
