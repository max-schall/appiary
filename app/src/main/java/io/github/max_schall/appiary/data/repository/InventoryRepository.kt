package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.InventoryDao
import io.github.max_schall.appiary.data.entity.InventoryItemEntity
import io.github.max_schall.appiary.domain.model.InventoryCategory
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val dao: InventoryDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeAll(): Flow<List<InventoryItemEntity>> = dao.observeAll()

    suspend fun save(item: InventoryItemEntity) = dao.upsert(item.copy(updatedAt = clock()))

    suspend fun add(
        name: String,
        category: InventoryCategory,
        quantity: Double = 0.0,
        unit: String? = null,
        lowStockThreshold: Double? = null,
        notes: String? = null,
    ): String {
        val now = clock()
        val item = InventoryItemEntity(
            id = newId(), name = name.trim(), category = category, quantity = quantity,
            unit = unit?.trim()?.ifBlank { null }, lowStockThreshold = lowStockThreshold,
            notes = notes?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now,
        )
        dao.upsert(item)
        return item.id
    }

    /** Bump stock up or down, clamped at zero — used by the +/- buttons. */
    suspend fun adjustQuantity(item: InventoryItemEntity, delta: Double) {
        val next = (item.quantity + delta).coerceAtLeast(0.0)
        dao.upsert(item.copy(quantity = next, updatedAt = clock()))
    }

    suspend fun delete(item: InventoryItemEntity) = dao.delete(item)
}
