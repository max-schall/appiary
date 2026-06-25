package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.dao.InspectionDao
import io.github.max_schall.appiary.data.entity.HiveStatusSnapshotEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.domain.usecase.DeriveHiveState
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.Flow

/**
 * Saving an inspection is the app's core write: it stores the inspection, rolls
 * the result into the hive's cached state, and appends a status snapshot — all
 * so the Today screen and rules engine can react immediately.
 */
class InspectionRepository(
    private val inspectionDao: InspectionDao,
    private val hiveDao: HiveDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeByHive(hiveId: String): Flow<List<InspectionEntity>> =
        inspectionDao.observeByHive(hiveId)

    suspend fun latestForHive(hiveId: String): InspectionEntity? =
        inspectionDao.latestForHive(hiveId)

    suspend fun save(inspection: InspectionEntity) {
        val now = clock()
        inspectionDao.upsert(inspection.copy(updatedAt = now))

        val hive = hiveDao.getHive(inspection.hiveId) ?: return
        val updated = DeriveHiveState.apply(hive, inspection, now)
        hiveDao.upsert(updated)

        hiveDao.upsertSnapshot(
            HiveStatusSnapshotEntity(
                id = newId(),
                hiveId = updated.id,
                takenAt = inspection.performedAt,
                status = updated.status,
                queenStatus = updated.queenStatus,
                broodPattern = updated.broodPattern,
                strength = updated.strength,
                temperament = updated.temperament,
                foodStores = updated.foodStores,
                source = "inspection:${inspection.id}",
                createdAt = now,
            ),
        )
    }

    suspend fun delete(inspection: InspectionEntity) = inspectionDao.delete(inspection)
}
