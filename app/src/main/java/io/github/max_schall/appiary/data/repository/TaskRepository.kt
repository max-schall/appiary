package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.ManualTaskDao
import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.domain.model.TaskStatus
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val dao: ManualTaskDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeAll(): Flow<List<ManualTaskEntity>> = dao.observeAll()
    fun observeOpen(): Flow<List<ManualTaskEntity>> = dao.observeOpen()
    fun observeByHive(hiveId: String): Flow<List<ManualTaskEntity>> = dao.observeByHive(hiveId)

    suspend fun getOpenTasks(): List<ManualTaskEntity> = dao.getOpenTasks()

    suspend fun create(
        title: String,
        details: String? = null,
        hiveId: String? = null,
        apiaryId: String? = null,
        dueAt: Long? = null,
    ): String {
        val now = clock()
        val task = ManualTaskEntity(
            id = newId(), title = title, details = details, hiveId = hiveId,
            apiaryId = apiaryId, dueAt = dueAt, createdAt = now, updatedAt = now,
        )
        dao.upsert(task)
        return task.id
    }

    suspend fun save(task: ManualTaskEntity) = dao.upsert(task.copy(updatedAt = clock()))

    suspend fun complete(task: ManualTaskEntity) {
        val now = clock()
        dao.upsert(task.copy(status = TaskStatus.DONE, completedAt = now, updatedAt = now))
    }

    suspend fun snooze(task: ManualTaskEntity, until: Long) {
        dao.upsert(task.copy(status = TaskStatus.SNOOZED, snoozedUntil = until, updatedAt = clock()))
    }

    suspend fun dismiss(task: ManualTaskEntity) {
        dao.upsert(task.copy(status = TaskStatus.DISMISSED, updatedAt = clock()))
    }

    suspend fun delete(task: ManualTaskEntity) = dao.delete(task)
}
