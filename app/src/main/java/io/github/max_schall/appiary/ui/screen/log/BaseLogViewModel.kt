package io.github.max_schall.appiary.ui.screen.log

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.ui.model.HiveOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Shared plumbing for every logging flow: the list of selectable hives, the
 * currently chosen hive (pre-filled from the nav arg when launched from a hive),
 * and a one-shot "saved" signal. Subclasses add their own form state and call
 * [saveWith] to persist + refresh recommendations + signal completion.
 */
abstract class BaseLogViewModel(
    savedStateHandle: SavedStateHandle,
    hiveRepo: HiveRepository,
    apiaryRepo: ApiaryRepository,
    private val refreshRecommendations: RefreshRecommendations,
    protected val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    val hiveOptions: StateFlow<List<HiveOption>> = combine(
        hiveRepo.observeAll(),
        apiaryRepo.observeApiaries(),
    ) { hives, apiaries ->
        val names = apiaries.associate { it.id to it.name }
        hives.map { HiveOption(it.id, it.name, it.apiaryId, names[it.apiaryId]) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedHiveId = MutableStateFlow(savedStateHandle.get<String>("hiveId"))
    val selectedHiveId: StateFlow<String?> = _selectedHiveId.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun selectHive(id: String) { _selectedHiveId.value = id }

    /**
     * Run [block] with the selected hive's id and apiary id, then refresh
     * recommendations and mark the flow complete. No-op if no hive is selected.
     */
    protected fun saveWith(block: suspend (hiveId: String, apiaryId: String) -> Unit) {
        val hiveId = _selectedHiveId.value ?: return
        val apiaryId = hiveOptions.value.firstOrNull { it.id == hiveId }?.apiaryId ?: return
        viewModelScope.launch {
            block(hiveId, apiaryId)
            refreshRecommendations()
            _saved.value = true
        }
    }
}
