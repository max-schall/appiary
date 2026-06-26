package io.github.max_schall.appiary.inventory

import io.github.max_schall.appiary.data.dao.InventoryDao
import io.github.max_schall.appiary.data.entity.InventoryItemEntity
import io.github.max_schall.appiary.data.repository.InventoryRepository
import io.github.max_schall.appiary.domain.model.InventoryCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeInventoryDao(seed: List<InventoryItemEntity> = emptyList()) : InventoryDao {
    val items = seed.associateBy { it.id }.toMutableMap()
    override suspend fun upsert(item: InventoryItemEntity) { items[item.id] = item }
    override suspend fun upsertAll(items: List<InventoryItemEntity>) { items.forEach { this.items[it.id] = it } }
    override suspend fun delete(item: InventoryItemEntity) { items.remove(item.id) }
    override fun observeAll(): Flow<List<InventoryItemEntity>> = flowOf(items.values.toList())
    override suspend fun getAll(): List<InventoryItemEntity> = items.values.toList()
}

class InventoryRepositoryTest {

    @Test
    fun `add trims fields and blanks become null`() = runTest {
        val dao = FakeInventoryDao()
        val repo = InventoryRepository(dao, clock = { 100L })

        val id = repo.add(
            name = "  Deep boxes ", category = InventoryCategory.HARDWARE,
            quantity = 8.0, unit = "  ", lowStockThreshold = 2.0, notes = "  ",
        )

        val item = dao.items[id]!!
        assertEquals("Deep boxes", item.name)
        assertEquals(8.0, item.quantity, 0.0)
        assertNull(item.unit)
        assertNull(item.notes)
        assertEquals(100L, item.createdAt)
    }

    @Test
    fun `adjustQuantity clamps at zero`() = runTest {
        val dao = FakeInventoryDao()
        val repo = InventoryRepository(dao, clock = { 1L })
        val id = repo.add("Frames", InventoryCategory.FRAMES, quantity = 1.0)
        val item = dao.items[id]!!

        repo.adjustQuantity(item, -5.0)

        assertEquals(0.0, dao.items[id]!!.quantity, 0.0)
    }

    @Test
    fun `isLow reflects the threshold`() {
        val base = InventoryItemEntity(
            name = "Sugar", category = InventoryCategory.FEED,
            quantity = 3.0, lowStockThreshold = 5.0, createdAt = 0, updatedAt = 0,
        )
        assertTrue(base.isLow)
        assertFalse(base.copy(quantity = 6.0).isLow)
        // No threshold set -> never flagged.
        assertFalse(base.copy(lowStockThreshold = null).isLow)
        // At the threshold counts as low.
        assertTrue(base.copy(quantity = 5.0).isLow)
    }
}
