package io.github.max_schall.appiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.max_schall.appiary.data.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Upsert suspend fun upsert(entry: WeightEntryEntity)
    @Upsert suspend fun upsertAll(items: List<WeightEntryEntity>)
    @Delete suspend fun delete(entry: WeightEntryEntity)

    @Query("SELECT * FROM weight_entries WHERE hiveId = :hiveId ORDER BY recordedAt DESC")
    fun observeByHive(hiveId: String): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries") suspend fun getAll(): List<WeightEntryEntity>
}
