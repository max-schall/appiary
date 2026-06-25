package io.github.max_schall.appiary.ui.screen.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.RecommendationRepository
import io.github.max_schall.appiary.data.repository.TaskRepository
import io.github.max_schall.appiary.domain.model.TaskStatus
import io.github.max_schall.appiary.domain.rules.rank
import io.github.max_schall.appiary.ui.model.RecommendationItem
import io.github.max_schall.appiary.util.TimeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TasksUiState(
    val recommendations: List<RecommendationItem> = emptyList(),
    val openTasks: List<ManualTaskEntity> = emptyList(),
    val doneTasks: List<ManualTaskEntity> = emptyList(),
)

class TasksViewModel(
    private val taskRepo: TaskRepository,
    private val recommendationRepo: RecommendationRepository,
    hiveRepo: HiveRepository,
    apiaryRepo: ApiaryRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    val uiState = combine(
        recommendationRepo.observeActive(),
        taskRepo.observeAll(),
        hiveRepo.observeAll(),
        apiaryRepo.observeApiaries(),
    ) { recs, tasks, hives, apiaries ->
        val hiveNames = hives.associate { it.id to it.name }
        val apiaryNames = apiaries.associate { it.id to it.name }
        TasksUiState(
            recommendations = recs
                .sortedWith(compareBy({ it.urgencyBucket.rank }, { -it.urgencyScore }))
                .map { it.toItem(hiveNames, apiaryNames) },
            openTasks = tasks.filter { it.status == TaskStatus.OPEN }
                .sortedWith(compareBy({ it.dueAt == null }, { it.dueAt })),
            doneTasks = tasks.filter { it.status == TaskStatus.DONE }
                .sortedByDescending { it.completedAt ?: it.updatedAt }
                .take(20),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    private fun GeneratedRecommendationEntity.toItem(
        hiveNames: Map<String, String>,
        apiaryNames: Map<String, String>,
    ) = RecommendationItem(this, hiveId?.let(hiveNames::get), apiaryId?.let(apiaryNames::get))

    fun addTask(title: String, remind: Boolean) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepo.create(title = title.trim(), dueAt = if (remind) clock() + TimeUtil.days(3) else null)
        }
    }

    fun completeTask(task: ManualTaskEntity) = viewModelScope.launch { taskRepo.complete(task) }
    fun snoozeTask(task: ManualTaskEntity) =
        viewModelScope.launch { taskRepo.snooze(task, clock() + TimeUtil.days(3)) }
    fun reopenTask(task: ManualTaskEntity) =
        viewModelScope.launch { taskRepo.save(task.copy(status = TaskStatus.OPEN, completedAt = null)) }
    fun deleteTask(task: ManualTaskEntity) = viewModelScope.launch { taskRepo.delete(task) }

    fun completeRec(id: String) = viewModelScope.launch { recommendationRepo.complete(id) }
    fun snoozeRec(id: String) = viewModelScope.launch { recommendationRepo.snooze(id, clock() + TimeUtil.days(3)) }
    fun dismissRec(id: String) = viewModelScope.launch { recommendationRepo.dismiss(id) }
}
