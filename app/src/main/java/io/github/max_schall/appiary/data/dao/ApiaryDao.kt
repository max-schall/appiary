package io.github.max_schall.appiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.max_schall.appiary.data.entity.ApiaryEntity
import io.github.max_schall.appiary.data.entity.ApiarySiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiaryDao {

    @Upsert suspend fun upsertSite(site: ApiarySiteEntity)
    @Upsert suspend fun upsertSites(sites: List<ApiarySiteEntity>)
    @Query("SELECT * FROM apiary_sites ORDER BY name")
    fun observeSites(): Flow<List<ApiarySiteEntity>>

    @Upsert suspend fun upsert(apiary: ApiaryEntity)
    @Upsert suspend fun upsertAll(apiaries: List<ApiaryEntity>)
    @Delete suspend fun delete(apiary: ApiaryEntity)

    @Query("SELECT * FROM apiaries ORDER BY name")
    fun observeApiaries(): Flow<List<ApiaryEntity>>

    @Query("SELECT * FROM apiaries WHERE id = :id")
    fun observeApiary(id: String): Flow<ApiaryEntity?>

    @Query("SELECT * FROM apiaries WHERE id = :id")
    suspend fun getApiary(id: String): ApiaryEntity?

    @Query("SELECT COUNT(*) FROM apiaries")
    suspend fun count(): Int

    @Query("SELECT * FROM apiary_sites") suspend fun getAllSites(): List<ApiarySiteEntity>
    @Query("SELECT * FROM apiaries") suspend fun getAllApiaries(): List<ApiaryEntity>
    @Query("SELECT * FROM apiary_sites WHERE id = :id") suspend fun getSite(id: String): ApiarySiteEntity?

    /**
     * Apiaries with hive count, active-recommendation count, and most recent
     * inspection time. Active recommendations exclude dismissed/completed.
     */
    @Query(
        """
        SELECT a.id AS id,
               a.name AS name,
               (SELECT COUNT(*) FROM hives h WHERE h.apiaryId = a.id AND h.archived = 0) AS hiveCount,
               (SELECT COUNT(*) FROM recommendations r
                    WHERE r.apiaryId = a.id AND r.status = 'ACTIVE') AS openRecommendationCount,
               (SELECT MAX(i.performedAt) FROM inspections i WHERE i.apiaryId = a.id) AS lastVisitAt
        FROM apiaries a
        ORDER BY a.name
        """
    )
    fun observeApiaryStats(): Flow<List<ApiaryStats>>
}
