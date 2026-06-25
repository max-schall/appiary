package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.ColonyEventDao
import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.entity.ColonyEventEntity
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.domain.model.ColonyEventType
import io.github.max_schall.appiary.domain.model.HiveOrigin
import io.github.max_schall.appiary.domain.model.HiveStatus
import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.Flow

/**
 * Structural colony operations and their lineage/event records:
 *  - [split] spawns a daughter hive from a parent (one side is left queenless);
 *  - [captureSwarm] creates a fresh hive of swarm origin;
 *  - [merge] folds a (usually weak) colony into another, archiving the source.
 *
 * Each writes a [ColonyEventEntity] so the operation shows on both hives' timelines.
 */
class ColonyRepository(
    private val colonyDao: ColonyEventDao,
    private val hiveDao: HiveDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeForHive(hiveId: String): Flow<List<ColonyEventEntity>> = colonyDao.observeForHive(hiveId)

    /**
     * Create a daughter colony from [parentId]. The daughter is queenless when the
     * old queen stays with the parent ([daughterTakesQueen] = false). Returns the
     * new hive id, or null if the parent is missing.
     */
    suspend fun split(
        parentId: String,
        daughterName: String,
        daughterTakesQueen: Boolean,
        occurredAt: Long = clock(),
        notes: String? = null,
    ): String? {
        val parent = hiveDao.getHive(parentId) ?: return null
        val now = clock()
        val daughter = HiveEntity(
            id = newId(),
            apiaryId = parent.apiaryId,
            name = daughterName,
            queenStatus = if (daughterTakesQueen) QueenStatus.QUEENRIGHT else QueenStatus.QUEENLESS,
            installedAt = occurredAt,
            originType = HiveOrigin.SPLIT,
            parentHiveId = parent.id,
            createdAt = now,
            updatedAt = now,
        )
        hiveDao.upsert(daughter)
        // The side without the queen is now queenless; keep the parent consistent.
        if (daughterTakesQueen) {
            hiveDao.upsert(parent.copy(queenStatus = QueenStatus.QUEENLESS, updatedAt = now))
        }
        record(ColonyEventType.SPLIT, daughter.id, daughter.apiaryId, parent.id, parent.name, occurredAt, notes)
        return daughter.id
    }

    /** Create a new colony from a captured swarm, hived at [apiaryId]. */
    suspend fun captureSwarm(
        apiaryId: String,
        name: String,
        occurredAt: Long = clock(),
        notes: String? = null,
    ): String {
        val now = clock()
        val hive = HiveEntity(
            id = newId(),
            apiaryId = apiaryId,
            name = name,
            queenStatus = QueenStatus.UNCERTAIN,
            installedAt = occurredAt,
            originType = HiveOrigin.SWARM,
            createdAt = now,
            updatedAt = now,
        )
        hiveDao.upsert(hive)
        record(ColonyEventType.SWARM_CAPTURE, hive.id, apiaryId, null, null, occurredAt, notes)
        return hive.id
    }

    /**
     * Fold [sourceId] into [targetId]: the source colony is archived (its history is
     * preserved) and a merge event is recorded on the surviving target.
     */
    suspend fun merge(
        sourceId: String,
        targetId: String,
        occurredAt: Long = clock(),
        notes: String? = null,
    ) {
        val source = hiveDao.getHive(sourceId) ?: return
        val target = hiveDao.getHive(targetId) ?: return
        val now = clock()
        hiveDao.upsert(source.copy(status = HiveStatus.DEAD, archived = true, updatedAt = now))
        record(ColonyEventType.MERGE, target.id, target.apiaryId, source.id, source.name, occurredAt, notes)
    }

    private suspend fun record(
        type: ColonyEventType,
        hiveId: String,
        apiaryId: String,
        relatedHiveId: String?,
        relatedHiveName: String?,
        occurredAt: Long,
        notes: String?,
    ) {
        val now = clock()
        colonyDao.upsert(
            ColonyEventEntity(
                hiveId = hiveId, apiaryId = apiaryId, type = type,
                relatedHiveId = relatedHiveId, relatedHiveName = relatedHiveName,
                occurredAt = occurredAt, notes = notes, createdAt = now, updatedAt = now,
            ),
        )
    }
}
