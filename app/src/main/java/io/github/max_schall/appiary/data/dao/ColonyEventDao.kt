package io.github.max_schall.appiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.max_schall.appiary.data.entity.ColonyEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ColonyEventDao {
    @Upsert suspend fun upsert(event: ColonyEventEntity)
    @Upsert suspend fun upsertAll(items: List<ColonyEventEntity>)
    @Delete suspend fun delete(event: ColonyEventEntity)

    /** Events that involve a hive as either the subject or the related party. */
    @Query("SELECT * FROM colony_events WHERE hiveId = :hiveId OR relatedHiveId = :hiveId ORDER BY occurredAt DESC")
    fun observeForHive(hiveId: String): Flow<List<ColonyEventEntity>>

    @Query("SELECT * FROM colony_events") suspend fun getAll(): List<ColonyEventEntity>
}
