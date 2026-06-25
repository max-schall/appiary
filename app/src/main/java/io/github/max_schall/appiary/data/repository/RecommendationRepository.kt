package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.RecommendationDao
import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.domain.model.RecommendationStatus
import kotlinx.coroutines.flow.Flow

class RecommendationRepository(
    private val dao: RecommendationDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeActive(): Flow<List<GeneratedRecommendationEntity>> = dao.observeActive()
    fun observeActiveByApiary(apiaryId: String): Flow<List<GeneratedRecommendationEntity>> =
        dao.observeActiveByApiary(apiaryId)
    fun observeActiveByHive(hiveId: String): Flow<List<GeneratedRecommendationEntity>> =
        dao.observeActiveByHive(hiveId)

    suspend fun getById(id: String): GeneratedRecommendationEntity? = dao.getById(id)
    suspend fun getAll(): List<GeneratedRecommendationEntity> = dao.getAll()

    /** Compact snapshot for the home-screen widget: bucket counts + top item. */
    suspend fun widgetSnapshot(): WidgetSnapshot = WidgetSnapshot(
        doNow = dao.countActiveInBucket(io.github.max_schall.appiary.domain.model.UrgencyBucket.DO_NOW.name),
        dueSoon = dao.countActiveInBucket(io.github.max_schall.appiary.domain.model.UrgencyBucket.DUE_SOON.name),
        top = dao.topActive(),
    )

    suspend fun snooze(id: String, until: Long) =
        dao.updateStatus(id, RecommendationStatus.SNOOZED, until, clock())

    suspend fun dismiss(id: String) =
        dao.updateStatus(id, RecommendationStatus.DISMISSED, null, clock())

    suspend fun complete(id: String) =
        dao.updateStatus(id, RecommendationStatus.COMPLETED, null, clock())

    /** Direct upsert — used by the engine integration in Phase 3. */
    suspend fun upsertAll(items: List<GeneratedRecommendationEntity>) = dao.upsertAll(items)
}

/** Minimal data the home-screen widget needs. */
data class WidgetSnapshot(
    val doNow: Int,
    val dueSoon: Int,
    val top: GeneratedRecommendationEntity?,
)
