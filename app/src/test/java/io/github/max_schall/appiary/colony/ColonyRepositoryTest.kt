package io.github.max_schall.appiary.colony

import io.github.max_schall.appiary.data.dao.ColonyEventDao
import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.entity.ColonyEventEntity
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.HiveStatusSnapshotEntity
import io.github.max_schall.appiary.data.repository.ColonyRepository
import io.github.max_schall.appiary.domain.model.ColonyEventType
import io.github.max_schall.appiary.domain.model.HiveOrigin
import io.github.max_schall.appiary.domain.model.QueenStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeHiveDao(seed: List<HiveEntity> = emptyList()) : HiveDao {
    val hives = seed.associateBy { it.id }.toMutableMap()
    override suspend fun upsert(hive: HiveEntity) { hives[hive.id] = hive }
    override suspend fun upsertAll(hives: List<HiveEntity>) { hives.forEach { this.hives[it.id] = it } }
    override suspend fun delete(hive: HiveEntity) { hives.remove(hive.id) }
    override fun observeAll(): Flow<List<HiveEntity>> = flowOf(hives.values.toList())
    override fun observeByApiary(apiaryId: String): Flow<List<HiveEntity>> = flowOf(hives.values.filter { it.apiaryId == apiaryId })
    override fun observeHive(id: String): Flow<HiveEntity?> = flowOf(hives[id])
    override suspend fun getHive(id: String): HiveEntity? = hives[id]
    override suspend fun getByNfcTag(tagId: String): HiveEntity? = hives.values.firstOrNull { it.nfcTagId == tagId }
    override suspend fun getActiveHives(): List<HiveEntity> = hives.values.filter { !it.archived }
    override suspend fun setArchived(id: String, archived: Boolean, now: Long) { hives[id]?.let { hives[id] = it.copy(archived = archived) } }
    override suspend fun upsertSnapshot(snapshot: HiveStatusSnapshotEntity) {}
    override suspend fun upsertSnapshots(snapshots: List<HiveStatusSnapshotEntity>) {}
    override fun observeSnapshots(hiveId: String): Flow<List<HiveStatusSnapshotEntity>> = flowOf(emptyList())
    override suspend fun getAllHives(): List<HiveEntity> = hives.values.toList()
    override suspend fun getAllSnapshots(): List<HiveStatusSnapshotEntity> = emptyList()
}

private class FakeColonyDao : ColonyEventDao {
    val events = mutableListOf<ColonyEventEntity>()
    override suspend fun upsert(event: ColonyEventEntity) { events.add(event) }
    override suspend fun upsertAll(items: List<ColonyEventEntity>) { events.addAll(items) }
    override suspend fun delete(event: ColonyEventEntity) { events.remove(event) }
    override fun observeForHive(hiveId: String): Flow<List<ColonyEventEntity>> =
        flowOf(events.filter { it.hiveId == hiveId || it.relatedHiveId == hiveId })
    override suspend fun getAll(): List<ColonyEventEntity> = events.toList()
}

class ColonyRepositoryTest {

    private fun hive(id: String, queen: QueenStatus = QueenStatus.QUEENRIGHT) = HiveEntity(
        id = id, apiaryId = "a1", name = "Hive $id", queenStatus = queen,
        createdAt = 0, updatedAt = 0,
    )

    @Test
    fun `split without the queen leaves the daughter queenless and parent queenright`() = runTest {
        val hiveDao = FakeHiveDao(listOf(hive("parent")))
        val colonyDao = FakeColonyDao()
        val repo = ColonyRepository(colonyDao, hiveDao, clock = { 1000L })

        val daughterId = repo.split("parent", "Split A", daughterTakesQueen = false)

        assertNotNull(daughterId)
        val daughter = hiveDao.hives[daughterId]!!
        assertEquals(HiveOrigin.SPLIT, daughter.originType)
        assertEquals("parent", daughter.parentHiveId)
        assertEquals(QueenStatus.QUEENLESS, daughter.queenStatus)
        assertEquals(QueenStatus.QUEENRIGHT, hiveDao.hives["parent"]!!.queenStatus)
        val event = colonyDao.events.single()
        assertEquals(ColonyEventType.SPLIT, event.type)
        assertEquals(daughterId, event.hiveId)
        assertEquals("parent", event.relatedHiveId)
    }

    @Test
    fun `split taking the queen makes the parent queenless`() = runTest {
        val hiveDao = FakeHiveDao(listOf(hive("parent")))
        val repo = ColonyRepository(FakeColonyDao(), hiveDao)

        val daughterId = repo.split("parent", "Split B", daughterTakesQueen = true)

        assertEquals(QueenStatus.QUEENRIGHT, hiveDao.hives[daughterId]!!.queenStatus)
        assertEquals(QueenStatus.QUEENLESS, hiveDao.hives["parent"]!!.queenStatus)
    }

    @Test
    fun `split of a missing parent is a no-op`() = runTest {
        val hiveDao = FakeHiveDao()
        val colonyDao = FakeColonyDao()
        val repo = ColonyRepository(colonyDao, hiveDao)

        assertNull(repo.split("ghost", "X", daughterTakesQueen = false))
        assertTrue(colonyDao.events.isEmpty())
        assertTrue(hiveDao.hives.isEmpty())
    }

    @Test
    fun `captured swarm is a new swarm-origin hive with an event`() = runTest {
        val hiveDao = FakeHiveDao()
        val colonyDao = FakeColonyDao()
        val repo = ColonyRepository(colonyDao, hiveDao)

        val id = repo.captureSwarm("a1", "Bait box")

        val hive = hiveDao.hives[id]!!
        assertEquals(HiveOrigin.SWARM, hive.originType)
        assertEquals(QueenStatus.UNCERTAIN, hive.queenStatus)
        assertEquals(ColonyEventType.SWARM_CAPTURE, colonyDao.events.single().type)
    }

    @Test
    fun `merge archives the source and records the event on the target`() = runTest {
        val hiveDao = FakeHiveDao(listOf(hive("weak"), hive("strong")))
        val colonyDao = FakeColonyDao()
        val repo = ColonyRepository(colonyDao, hiveDao)

        repo.merge(sourceId = "weak", targetId = "strong")

        assertTrue(hiveDao.hives["weak"]!!.archived)
        assertTrue(!hiveDao.hives["strong"]!!.archived)
        val event = colonyDao.events.single()
        assertEquals(ColonyEventType.MERGE, event.type)
        assertEquals("strong", event.hiveId)
        assertEquals("weak", event.relatedHiveId)
    }
}
