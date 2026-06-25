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
import io.github.max_schall.appiary.data.entity.FeedingEventEntity
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.FeedingRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.domain.model.FeedType
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FeedingForm(
    val fedAt: Long,
    val feedType: FeedType = FeedType.SYRUP_LIGHT,
    val quantity: String = "",
    val unit: String = "L",
    val notes: String = "",
)

class FeedingViewModel(
    savedStateHandle: SavedStateHandle,
    hiveRepo: HiveRepository,
    apiaryRepo: ApiaryRepository,
    private val feedingRepo: FeedingRepository,
    refreshRecommendations: RefreshRecommendations,
) : BaseLogViewModel(savedStateHandle, hiveRepo, apiaryRepo, refreshRecommendations) {

    private val _form = MutableStateFlow(FeedingForm(fedAt = clock()))
    val form = _form.asStateFlow()
    fun update(transform: (FeedingForm) -> FeedingForm) { _form.value = transform(_form.value) }

    fun save() = saveWith { hiveId, apiaryId ->
        val f = _form.value
        val now = clock()
        feedingRepo.save(
            FeedingEventEntity(
                id = newId(), hiveId = hiveId, apiaryId = apiaryId, fedAt = f.fedAt,
                feedType = f.feedType, quantity = f.quantity.toDoubleOrNull(),
                unit = f.unit.ifBlank { null }, notes = f.notes.ifBlank { null },
                createdAt = now, updatedAt = now,
            ),
        )
    }
}

@Composable
fun FeedingScreen(
    onDone: () -> Unit,
    viewModel: FeedingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val hiveOptions by viewModel.hiveOptions.collectAsStateWithLifecycle()
    val selectedHiveId by viewModel.selectedHiveId.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    LaunchedEffect(saved) { if (saved) onDone() }

    LogScaffold(stringResource(R.string.log_feeding), onDone, viewModel::save, selectedHiveId != null) {
        if (selectedHiveId == null || hiveOptions.size > 1) {
            HivePickerField(hiveOptions, selectedHiveId, viewModel::selectHive)
        }
        DateField(stringResource(R.string.field_date), form.fedAt, { v -> viewModel.update { it.copy(fedAt = v) } })
        ChipChoice(stringResource(R.string.field_feed_type), FeedType.entries, form.feedType, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(feedType = v) }
        }
        DecimalField(stringResource(R.string.field_quantity), form.quantity) { v -> viewModel.update { it.copy(quantity = v) } }
        OutlinedTextField(
            value = form.unit,
            onValueChange = { v -> viewModel.update { it.copy(unit = v) } },
            label = { Text(stringResource(R.string.field_unit)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        NotesField(form.notes, { v -> viewModel.update { it.copy(notes = v) } })
    }
}

@Composable
internal fun DecimalField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    LabeledField(label, modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { input -> onChange(input.filter { it.isDigit() || it == '.' }) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
