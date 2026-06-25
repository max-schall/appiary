package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.WeightDao
import io.github.max_schall.appiary.data.entity.WeightEntryEntity
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.Flow

class WeightRepository(
    private val dao: WeightDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeByHive(hiveId: String): Flow<List<WeightEntryEntity>> = dao.observeByHive(hiveId)

    suspend fun save(entry: WeightEntryEntity) = dao.upsert(entry.copy(updatedAt = clock()))

    /** Convenience for the quick "weigh" dialog. */
    suspend fun add(hiveId: String, apiaryId: String, weightKg: Double, recordedAt: Long = clock(), notes: String? = null) {
        val now = clock()
        dao.upsert(
            WeightEntryEntity(
                id = newId(), hiveId = hiveId, apiaryId = apiaryId,
                recordedAt = recordedAt, weightKg = weightKg, notes = notes,
                createdAt = now, updatedAt = now,
            ),
        )
    }
}
