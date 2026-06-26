package io.github.max_schall.appiary.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.max_schall.appiary.domain.model.InventoryCategory
import io.github.max_schall.appiary.util.newId
import kotlinx.serialization.Serializable

/**
 * A line item in the beekeeper's equipment/supply inventory — boxes, frames,
 * tools, feed, treatment stock, jars, etc. Inventory is kept at the operation
 * level (not per-hive), so there is no foreign key; [quantity] is a Double so the
 * same model covers countable items (10 boxes) and bulk amounts (4.5 kg sugar).
 * When [lowStockThreshold] is set the UI flags the item once stock falls to it.
 */
@Entity(
    tableName = "inventory_items",
    indices = [Index("category"), Index("name")],
)
@Serializable
data class InventoryItemEntity(
    @PrimaryKey val id: String = newId(),
    val name: String,
    val category: InventoryCategory = InventoryCategory.HARDWARE,
    val quantity: Double = 0.0,
    val unit: String? = null,
    val lowStockThreshold: Double? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
) {
    /** True when a threshold is set and stock has fallen to or below it. */
    val isLow: Boolean get() = lowStockThreshold?.let { quantity <= it } ?: false
}
