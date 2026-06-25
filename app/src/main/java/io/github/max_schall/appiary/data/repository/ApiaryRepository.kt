package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.ApiaryDao
import io.github.max_schall.appiary.data.dao.ApiaryStats
import io.github.max_schall.appiary.data.entity.ApiaryEntity
import io.github.max_schall.appiary.data.entity.ApiarySiteEntity
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.Flow

class ApiaryRepository(
    private val dao: ApiaryDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeApiaries(): Flow<List<ApiaryEntity>> = dao.observeApiaries()
    fun observeApiary(id: String): Flow<ApiaryEntity?> = dao.observeApiary(id)
    fun observeApiaryStats(): Flow<List<ApiaryStats>> = dao.observeApiaryStats()
    fun observeSites(): Flow<List<ApiarySiteEntity>> = dao.observeSites()

    suspend fun getApiary(id: String): ApiaryEntity? = dao.getApiary(id)

    suspend fun createApiary(name: String, siteId: String? = null, notes: String? = null): String {
        val now = clock()
        val entity = ApiaryEntity(
            id = newId(), name = name, siteId = siteId, notes = notes,
            createdAt = now, updatedAt = now,
        )
        dao.upsert(entity)
        return entity.id
    }

    suspend fun updateApiary(apiary: ApiaryEntity) =
        dao.upsert(apiary.copy(updatedAt = clock()))

    suspend fun deleteApiary(apiary: ApiaryEntity) = dao.delete(apiary)

    /** Set an apiary's location (creating/linking a site) with a resolved country. */
    suspend fun setLocation(apiaryId: String, latitude: Double, longitude: Double, countryCode: String?) {
        val now = clock()
        val apiary = dao.getApiary(apiaryId) ?: return
        val existing = apiary.siteId?.let { dao.getSite(it) }
        if (existing == null) {
            val site = ApiarySiteEntity(
                name = apiary.name, latitude = latitude, longitude = longitude,
                countryCode = countryCode, createdAt = now, updatedAt = now,
            )
            dao.upsertSite(site)
            dao.upsert(apiary.copy(siteId = site.id, updatedAt = now))
        } else {
            dao.upsertSite(
                existing.copy(latitude = latitude, longitude = longitude, countryCode = countryCode, updatedAt = now),
            )
        }
    }

    suspend fun siteForApiary(apiaryId: String): ApiarySiteEntity? =
        dao.getApiary(apiaryId)?.siteId?.let { dao.getSite(it) }

    suspend fun createSite(name: String, latitude: Double? = null, longitude: Double? = null): String {
        val now = clock()
        val site = ApiarySiteEntity(
            id = newId(), name = name, latitude = latitude, longitude = longitude,
            createdAt = now, updatedAt = now,
        )
        dao.upsertSite(site)
        return site.id
    }
}
