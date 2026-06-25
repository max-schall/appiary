package io.github.max_schall.appiary.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.max_schall.appiary.data.entity.ClimateCacheEntity

@Dao
interface ClimateCacheDao {
    @Upsert suspend fun upsert(entry: ClimateCacheEntity)

    @Query("SELECT * FROM climate_cache WHERE locationKey = :key")
    suspend fun get(key: String): ClimateCacheEntity?
}
