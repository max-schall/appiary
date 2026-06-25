package io.github.max_schall.appiary.ui.screen.log

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.MiteCheckRepository
import io.github.max_schall.appiary.domain.model.MiteCheckMethod
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MiteCheckForm(
    val checkedAt: Long,
    val method: MiteCheckMethod = MiteCheckMethod.ALCOHOL_WASH,
    val sampleSize: String = "300",
    val mitesCounted: String = "",
    val notes: String = "",
)

class MiteCheckViewModel(
    savedStateHandle: SavedStateHandle,
    hiveRepo: HiveRepository,
    apiaryRepo: ApiaryRepository,
    private val miteCheckRepo: MiteCheckRepository,
    refreshRecommendations: RefreshRecommendations,
) : BaseLogViewModel(savedStateHandle, hiveRepo, apiaryRepo, refreshRecommendations) {

    private val _form = MutableStateFlow(MiteCheckForm(checkedAt = clock()))
    val form = _form.asStateFlow()
    fun update(transform: (MiteCheckForm) -> MiteCheckForm) { _form.value = transform(_form.value) }

    fun save() = saveWith { hiveId, apiaryId ->
        val f = _form.value
        val now = clock()
        miteCheckRepo.save(
            MiteCheckEntity(
                id = newId(), hiveId = hiveId, apiaryId = apiaryId, checkedAt = f.checkedAt,
                method = f.method, sampleSize = f.sampleSize.toIntOrNull(),
                mitesCounted = f.mitesCounted.toIntOrNull(),
                notes = f.notes.ifBlank { null }, createdAt = now, updatedAt = now,
            ),
        )
    }
}

@Composable
fun MiteCheckScreen(
    onDone: () -> Unit,
    viewModel: MiteCheckViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val hiveOptions by viewModel.hiveOptions.collectAsStateWithLifecycle()
    val selectedHiveId by viewModel.selectedHiveId.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    LaunchedEffect(saved) { if (saved) onDone() }

    LogScaffold(stringResource(R.string.log_mite), onDone, viewModel::save, selectedHiveId != null) {
        if (selectedHiveId == null || hiveOptions.size > 1) {
            HivePickerField(hiveOptions, selectedHiveId, viewModel::selectHive)
        }
        DateField(stringResource(R.string.field_date), form.checkedAt, { v -> viewModel.update { it.copy(checkedAt = v) } })
        ChipChoice(stringResource(R.string.field_method), MiteCheckMethod.entries, form.method, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(method = v) }
        }
        NumberField(stringResource(R.string.field_sample_size), form.sampleSize) { v -> viewModel.update { it.copy(sampleSize = v) } }
        NumberField(stringResource(R.string.field_mites_counted), form.mitesCounted) { v -> viewModel.update { it.copy(mitesCounted = v) } }
        NotesField(form.notes, { v -> viewModel.update { it.copy(notes = v) } })
    }
}

@Composable
internal fun NumberField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    LabeledField(label, modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onChange(it.filter(Char::isDigit)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
