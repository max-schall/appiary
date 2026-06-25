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
import io.github.max_schall.appiary.data.entity.TreatmentEventEntity
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.TreatmentRepository
import io.github.max_schall.appiary.domain.model.TreatmentType
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.util.TimeUtil
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TreatmentForm(
    val startedAt: Long,
    val type: TreatmentType = TreatmentType.OXALIC_ACID,
    val finished: Boolean = false,
    val dosage: String = "",
    val scheduleFollowUp: Boolean = true,
    val notes: String = "",
)

class TreatmentViewModel(
    savedStateHandle: SavedStateHandle,
    hiveRepo: HiveRepository,
    apiaryRepo: ApiaryRepository,
    private val treatmentRepo: TreatmentRepository,
    refreshRecommendations: RefreshRecommendations,
) : BaseLogViewModel(savedStateHandle, hiveRepo, apiaryRepo, refreshRecommendations) {

    private val _form = MutableStateFlow(TreatmentForm(startedAt = clock()))
    val form = _form.asStateFlow()
    fun update(transform: (TreatmentForm) -> TreatmentForm) { _form.value = transform(_form.value) }

    fun save() = saveWith { hiveId, apiaryId ->
        val f = _form.value
        val now = clock()
        val endedAt = if (f.finished) now else null
        // Schedule the post-treatment efficacy check 14 days out, once finished.
        val followUp = if (f.finished && f.scheduleFollowUp) now + TimeUtil.days(14) else null
        treatmentRepo.save(
            TreatmentEventEntity(
                id = newId(), hiveId = hiveId, apiaryId = apiaryId, type = f.type,
                startedAt = f.startedAt, endedAt = endedAt, dosage = f.dosage.ifBlank { null },
                followUpCheckDueAt = followUp, notes = f.notes.ifBlank { null },
                createdAt = now, updatedAt = now,
            ),
        )
    }
}

@Composable
fun TreatmentScreen(
    onDone: () -> Unit,
    viewModel: TreatmentViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val hiveOptions by viewModel.hiveOptions.collectAsStateWithLifecycle()
    val selectedHiveId by viewModel.selectedHiveId.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    LaunchedEffect(saved) { if (saved) onDone() }

    LogScaffold(stringResource(R.string.log_treatment), onDone, viewModel::save, selectedHiveId != null) {
        if (selectedHiveId == null || hiveOptions.size > 1) {
            HivePickerField(hiveOptions, selectedHiveId, viewModel::selectHive)
        }
        ChipChoice(stringResource(R.string.field_type), TreatmentType.entries, form.type, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(type = v) }
        }
        DateField(stringResource(R.string.field_start_date), form.startedAt, { v -> viewModel.update { it.copy(startedAt = v) } })
        OutlinedTextField(
            value = form.dosage,
            onValueChange = { v -> viewModel.update { it.copy(dosage = v) } },
            label = { Text(stringResource(R.string.field_dosage)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        ToggleRow(stringResource(R.string.field_treatment_finished), form.finished, { v -> viewModel.update { it.copy(finished = v) } })
        if (form.finished) {
            ToggleRow(
                stringResource(R.string.field_schedule_check),
                form.scheduleFollowUp,
                { v -> viewModel.update { it.copy(scheduleFollowUp = v) } },
            )
        }
        NotesField(form.notes, { v -> viewModel.update { it.copy(notes = v) } })
    }
}
