package io.github.max_schall.appiary.ui.screen.log

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.TaskRepository
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.model.HiveOption
import io.github.max_schall.appiary.util.TimeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Quick note / reminder capture. Saved as a manual task so it can carry a due
 * date and feed the overdue-follow-up rule. A hive link is optional.
 */
class NoteViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
    hiveRepo: HiveRepository,
    apiaryRepo: ApiaryRepository,
    private val taskRepo: TaskRepository,
    private val refreshRecommendations: RefreshRecommendations,
    private val clock: () -> Long = System::currentTimeMillis,
) : AndroidViewModel(application) {

    private val general = HiveOption(
        id = "", hiveName = application.getString(R.string.general_note), apiaryId = "", apiaryName = null,
    )

    val hiveOptions = combine(hiveRepo.observeAll(), apiaryRepo.observeApiaries()) { hives, apiaries ->
        val names = apiaries.associate { it.id to it.name }
        listOf(general) + hives.map { HiveOption(it.id, it.name, it.apiaryId, names[it.apiaryId]) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf(general))

    val selectedHiveId = MutableStateFlow(savedStateHandle.get<String>("hiveId") ?: "")
    val text = MutableStateFlow("")
    val remind = MutableStateFlow(false)
    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    fun selectHive(id: String) { selectedHiveId.value = id }
    fun setText(v: String) { text.value = v }
    fun setRemind(v: Boolean) { remind.value = v }

    fun save() {
        val title = text.value.trim()
        if (title.isEmpty()) return
        val hiveId = selectedHiveId.value.ifBlank { null }
        val apiaryId = hiveOptions.value.firstOrNull { it.id == hiveId }?.apiaryId?.ifBlank { null }
        viewModelScope.launch {
            taskRepo.create(
                title = title, hiveId = hiveId, apiaryId = apiaryId,
                dueAt = if (remind.value) clock() + TimeUtil.days(3) else null,
            )
            refreshRecommendations()
            _saved.value = true
        }
    }
}

@Composable
fun NoteScreen(
    onDone: () -> Unit,
    viewModel: NoteViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val hiveOptions by viewModel.hiveOptions.collectAsStateWithLifecycle()
    val selectedHiveId by viewModel.selectedHiveId.collectAsStateWithLifecycle()
    val text by viewModel.text.collectAsStateWithLifecycle()
    val remind by viewModel.remind.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    LaunchedEffect(saved) { if (saved) onDone() }

    LogScaffold(stringResource(R.string.log_note_title), onDone, viewModel::save, text.isNotBlank()) {
        HivePickerField(hiveOptions, selectedHiveId, viewModel::selectHive)
        OutlinedTextField(
            value = text,
            onValueChange = viewModel::setText,
            label = { Text(stringResource(R.string.field_note)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        ToggleRow(stringResource(R.string.remind_3_days), remind, viewModel::setRemind)
    }
}
