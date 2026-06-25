package io.github.max_schall.appiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.domain.model.RecommendationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ManualTaskDao {
    @Upsert suspend fun upsert(task: ManualTaskEntity)
    @Upsert suspend fun upsertAll(items: List<ManualTaskEntity>)
    @Delete suspend fun delete(task: ManualTaskEntity)

    @Query("SELECT * FROM manual_tasks ORDER BY (dueAt IS NULL), dueAt ASC, createdAt DESC")
    fun observeAll(): Flow<List<ManualTaskEntity>>

    @Query("SELECT * FROM manual_tasks WHERE status = 'OPEN' ORDER BY (dueAt IS NULL), dueAt ASC")
    fun observeOpen(): Flow<List<ManualTaskEntity>>

    @Query("SELECT * FROM manual_tasks WHERE hiveId = :hiveId ORDER BY createdAt DESC")
    fun observeByHive(hiveId: String): Flow<List<ManualTaskEntity>>

    @Query("SELECT * FROM manual_tasks WHERE status = 'OPEN'")
    suspend fun getOpenTasks(): List<ManualTaskEntity>

    @Query("SELECT * FROM manual_tasks WHERE id = :id")
    suspend fun getTask(id: String): ManualTaskEntity?

    @Query("SELECT * FROM manual_tasks") suspend fun getAll(): List<ManualTaskEntity>
}

@Dao
interface RecommendationDao {
    @Upsert suspend fun upsert(rec: GeneratedRecommendationEntity)
    @Upsert suspend fun upsertAll(items: List<GeneratedRecommendationEntity>)
    @Delete suspend fun delete(rec: GeneratedRecommendationEntity)

    @Query("SELECT * FROM recommendations WHERE status = 'ACTIVE' ORDER BY urgencyScore DESC, dueAt ASC")
    fun observeActive(): Flow<List<GeneratedRecommendationEntity>>

    @Query("SELECT * FROM recommendations WHERE apiaryId = :apiaryId AND status = 'ACTIVE' ORDER BY urgencyScore DESC")
    fun observeActiveByApiary(apiaryId: String): Flow<List<GeneratedRecommendationEntity>>

    @Query("SELECT * FROM recommendations WHERE hiveId = :hiveId AND status = 'ACTIVE' ORDER BY urgencyScore DESC")
    fun observeActiveByHive(hiveId: String): Flow<List<GeneratedRecommendationEntity>>

    @Query("SELECT * FROM recommendations WHERE id = :id")
    suspend fun getById(id: String): GeneratedRecommendationEntity?

    /** All persisted recommendations, used by the engine to preserve user state. */
    @Query("SELECT * FROM recommendations")
    suspend fun getAll(): List<GeneratedRecommendationEntity>

    @Query("UPDATE recommendations SET status = :status, snoozedUntil = :snoozedUntil, updatedAt = :now WHERE id = :id")
    suspend fun updateStatus(id: String, status: RecommendationStatus, snoozedUntil: Long?, now: Long)

    /** Remove engine-owned rows not touched by the latest run (and not user-held). */
    @Query("DELETE FROM recommendations WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    /** Count of active recommendations in a bucket — used by the reminder worker. */
    @Query("SELECT COUNT(*) FROM recommendations WHERE status = 'ACTIVE' AND urgencyBucket = :bucket")
    suspend fun countActiveInBucket(bucket: String): Int

    /** Highest-priority active recommendation — used by the home-screen widget. */
    @Query("SELECT * FROM recommendations WHERE status = 'ACTIVE' ORDER BY urgencyScore DESC, dueAt ASC LIMIT 1")
    suspend fun topActive(): GeneratedRecommendationEntity?
}
