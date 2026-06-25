package io.github.max_schall.appiary.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.max_schall.appiary.util.newId
import kotlinx.serialization.Serializable

/**
 * Proof-of-purchase for a veterinary medicine (German Bestandsbuch, EU 2019/6
 * Art. 108): the supplier and a photographed receipt. One receipt can cover
 * several substances, so multiple [TreatmentEventEntity] rows may reference it
 * via `receiptId`.
 */
@Serializable
@Entity(tableName = "medicine_receipts")
data class MedicineReceiptEntity(
    @PrimaryKey val id: String = newId(),
    val photoUri: String? = null,
    val supplierName: String? = null,
    val supplierAddress: String? = null,
    val purchaseDate: Long? = null,
    val label: String? = null,
    val vetName: String? = null,
    val vetContact: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
