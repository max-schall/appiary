package io.github.max_schall.appiary.search

import io.github.max_schall.appiary.data.dao.FeedingDao
import io.github.max_schall.appiary.data.dao.HarvestDao
import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.dao.InspectionDao
import io.github.max_schall.appiary.data.dao.InventoryDao
import io.github.max_schall.appiary.data.dao.ManualTaskDao
import io.github.max_schall.appiary.data.dao.MiteCheckDao
import io.github.max_schall.appiary.data.dao.TreatmentDao
import io.github.max_schall.appiary.data.entity.FeedingEventEntity
import io.github.max_schall.appiary.data.entity.HarvestEventEntity
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.HiveStatusSnapshotEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.data.entity.InventoryItemEntity
import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.data.entity.TreatmentEventEntity
import io.github.max_schall.appiary.data.repository.SearchRepository
import io.github.max_schall.appiary.data.repository.SearchResultKind
import io.github.max_schall.appiary.domain.model.InventoryCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FHive(val seed: List<HiveEntity>) : HiveDao {
    override suspend fun upsert(hive: HiveEntity) {}
    override suspend fun upsertAll(hives: List<HiveEntity>) {}
    override suspend fun delete(hive: HiveEntity) {}
    override fun observeAll(): Flow<List<HiveEntity>> = flowOf(seed)
    override fun observeByApiary(apiaryId: String): Flow<List<HiveEntity>> = flowOf(emptyList())
    override fun observeHive(id: String): Flow<HiveEntity?> = flowOf(null)
    override suspend fun getHive(id: String): HiveEntity? = seed.firstOrNull { it.id == id }
    override suspend fun getByNfcTag(tagId: String): HiveEntity? = null
    override suspend fun getActiveHives(): List<HiveEntity> = seed
    override suspend fun setArchived(id: String, archived: Boolean, now: Long) {}
    override suspend fun upsertSnapshot(snapshot: HiveStatusSnapshotEntity) {}
    override suspend fun upsertSnapshots(snapshots: List<HiveStatusSnapshotEntity>) {}
    override fun observeSnapshots(hiveId: String): Flow<List<HiveStatusSnapshotEntity>> = flowOf(emptyList())
    override suspend fun getAllHives(): List<HiveEntity> = seed
    override suspend fun getAllSnapshots(): List<HiveStatusSnapshotEntity> = emptyList()
}

private class FInspection(val seed: List<InspectionEntity>) : InspectionDao {
    override suspend fun upsert(inspection: InspectionEntity) {}
    override suspend fun upsertAll(items: List<InspectionEntity>) {}
    override suspend fun delete(inspection: InspectionEntity) {}
    override fun observeByHive(hiveId: String): Flow<List<InspectionEntity>> = flowOf(emptyList())
    override suspend fun latestForHive(hiveId: String): InspectionEntity? = null
    override suspend fun recentForHive(hiveId: String, limit: Int): List<InspectionEntity> = emptyList()
    override fun observeAll(): Flow<List<InspectionEntity>> = flowOf(seed)
    override suspend fun getAll(): List<InspectionEntity> = seed
}

private class FMite : MiteCheckDao {
    override suspend fun upsert(check: MiteCheckEntity) {}
    override suspend fun upsertAll(items: List<MiteCheckEntity>) {}
    override suspend fun delete(check: MiteCheckEntity) {}
    override fun observeByHive(hiveId: String): Flow<List<MiteCheckEntity>> = flowOf(emptyList())
    override suspend fun latestForHive(hiveId: String): MiteCheckEntity? = null
    override fun observeAll(): Flow<List<MiteCheckEntity>> = flowOf(emptyList())
    override suspend fun getAll(): List<MiteCheckEntity> = emptyList()
}

private class FTreatment : TreatmentDao {
    override suspend fun upsert(event: TreatmentEventEntity) {}
    override suspend fun upsertAll(items: List<TreatmentEventEntity>) {}
    override suspend fun delete(event: TreatmentEventEntity) {}
    override fun observeByHive(hiveId: String): Flow<List<TreatmentEventEntity>> = flowOf(emptyList())
    override suspend fun latestForHive(hiveId: String): TreatmentEventEntity? = null
    override suspend fun getAll(): List<TreatmentEventEntity> = emptyList()
    override fun observeByApiary(apiaryId: String): Flow<List<TreatmentEventEntity>> = flowOf(emptyList())
    override suspend fun getByApiary(apiaryId: String): List<TreatmentEventEntity> = emptyList()
    override suspend fun getById(id: String): TreatmentEventEntity? = null
    override suspend fun countMissingReceipt(apiaryId: String): Int = 0
}

private class FFeeding : FeedingDao {
    override suspend fun upsert(event: FeedingEventEntity) {}
    override suspend fun upsertAll(items: List<FeedingEventEntity>) {}
    override suspend fun delete(event: FeedingEventEntity) {}
    override fun observeByHive(hiveId: String): Flow<List<FeedingEventEntity>> = flowOf(emptyList())
    override suspend fun getAll(): List<FeedingEventEntity> = emptyList()
}

private class FHarvest : HarvestDao {
    override suspend fun upsert(event: HarvestEventEntity) {}
    override suspend fun upsertAll(items: List<HarvestEventEntity>) {}
    override suspend fun delete(event: HarvestEventEntity) {}
    override fun observeByHive(hiveId: String): Flow<List<HarvestEventEntity>> = flowOf(emptyList())
    override fun observeByApiary(apiaryId: String): Flow<List<HarvestEventEntity>> = flowOf(emptyList())
    override fun observeAll(): Flow<List<HarvestEventEntity>> = flowOf(emptyList())
    override suspend fun getAll(): List<HarvestEventEntity> = emptyList()
}

private class FTask(val seed: List<ManualTaskEntity>) : ManualTaskDao {
    override suspend fun upsert(task: ManualTaskEntity) {}
    override suspend fun upsertAll(items: List<ManualTaskEntity>) {}
    override suspend fun delete(task: ManualTaskEntity) {}
    override fun observeAll(): Flow<List<ManualTaskEntity>> = flowOf(seed)
    override fun observeOpen(): Flow<List<ManualTaskEntity>> = flowOf(seed)
    override fun observeByHive(hiveId: String): Flow<List<ManualTaskEntity>> = flowOf(emptyList())
    override suspend fun getOpenTasks(): List<ManualTaskEntity> = seed
    override suspend fun getTask(id: String): ManualTaskEntity? = null
    override suspend fun getAll(): List<ManualTaskEntity> = seed
}

private class FInventory(val seed: List<InventoryItemEntity>) : InventoryDao {
    override suspend fun upsert(item: InventoryItemEntity) {}
    override suspend fun upsertAll(items: List<InventoryItemEntity>) {}
    override suspend fun delete(item: InventoryItemEntity) {}
    override fun observeAll(): Flow<List<InventoryItemEntity>> = flowOf(seed)
    override suspend fun getAll(): List<InventoryItemEntity> = seed
}

class SearchRepositoryTest {

    private fun hive(id: String, name: String) =
        HiveEntity(id = id, apiaryId = "a1", name = name, createdAt = 0, updatedAt = id.hashCode().toLong())

    private fun repo(
        hives: List<HiveEntity> = emptyList(),
        inspections: List<InspectionEntity> = emptyList(),
        tasks: List<ManualTaskEntity> = emptyList(),
        inventory: List<InventoryItemEntity> = emptyList(),
    ) = SearchRepository(
        FHive(hives), FInspection(inspections), FMite(), FTreatment(),
        FFeeding(), FHarvest(), FTask(tasks), FInventory(inventory),
    )

    @Test
    fun `short queries return nothing`() = runTest {
        val r = repo(hives = listOf(hive("h1", "Sunny")))
        assertTrue(r.search("S").isEmpty())
        assertTrue(r.search(" ").isEmpty())
    }

    @Test
    fun `matches hive names case-insensitively and carries hiveId`() = runTest {
        val r = repo(hives = listOf(hive("h1", "Sunny Meadow"), hive("h2", "Back Forty")))
        val hits = r.search("meadow")
        assertEquals(1, hits.size)
        assertEquals(SearchResultKind.HIVE, hits.single().kind)
        assertEquals("h1", hits.single().hiveId)
    }

    @Test
    fun `matches across types and resolves hive name`() = runTest {
        val r = repo(
            hives = listOf(hive("h1", "Sunny")),
            inspections = listOf(
                InspectionEntity(hiveId = "h1", apiaryId = "a1", performedAt = 10, notes = "queen looked weak", createdAt = 0, updatedAt = 0),
            ),
            tasks = listOf(
                ManualTaskEntity(title = "Order weak-hive feed", hiveId = "h1", createdAt = 5, updatedAt = 0),
            ),
            inventory = listOf(
                InventoryItemEntity(name = "Weak syrup", category = InventoryCategory.FEED, createdAt = 0, updatedAt = 0),
            ),
        )
        val hits = r.search("weak")
        val kinds = hits.map { it.kind }.toSet()
        assertEquals(setOf(SearchResultKind.INSPECTION, SearchResultKind.TASK, SearchResultKind.INVENTORY), kinds)
        // The inspection hit should know its hive's name.
        assertEquals("Sunny", hits.first { it.kind == SearchResultKind.INSPECTION }.hiveName)
        // Inventory has no hive link.
        assertEquals(null, hits.first { it.kind == SearchResultKind.INVENTORY }.hiveId)
    }
}
