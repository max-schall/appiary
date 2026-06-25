package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.HiveStatusSnapshotEntity
import kotlinx.coroutines.flow.Flow

class HiveRepository(
    private val dao: HiveDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeAll(): Flow<List<HiveEntity>> = dao.observeAll()
    fun observeByApiary(apiaryId: String): Flow<List<HiveEntity>> = dao.observeByApiary(apiaryId)
    fun observeHive(id: String): Flow<HiveEntity?> = dao.observeHive(id)
    fun observeSnapshots(hiveId: String): Flow<List<HiveStatusSnapshotEntity>> = dao.observeSnapshots(hiveId)

    suspend fun getHive(id: String): HiveEntity? = dao.getHive(id)
    suspend fun getActiveHives(): List<HiveEntity> = dao.getActiveHives()
    suspend fun getByNfcTag(tagId: String): HiveEntity? = dao.getByNfcTag(tagId)

    suspend fun createHive(apiaryId: String, name: String, installedAt: Long? = null): String {
        val now = clock()
        val hive = HiveEntity(
            apiaryId = apiaryId, name = name, installedAt = installedAt,
            createdAt = now, updatedAt = now,
        )
        dao.upsert(hive)
        return hive.id
    }

    /** Persist arbitrary hive edits (always bumps updatedAt). */
    suspend fun save(hive: HiveEntity) = dao.upsert(hive.copy(updatedAt = clock()))

    suspend fun archive(id: String, archived: Boolean = true) =
        dao.setArchived(id, archived, clock())
}
