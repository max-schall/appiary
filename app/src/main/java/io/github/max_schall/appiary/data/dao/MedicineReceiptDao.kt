package io.github.max_schall.appiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.max_schall.appiary.data.entity.MedicineReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineReceiptDao {
    @Upsert suspend fun upsert(receipt: MedicineReceiptEntity)
    @Upsert suspend fun upsertAll(items: List<MedicineReceiptEntity>)
    @Delete suspend fun delete(receipt: MedicineReceiptEntity)

    @Query("SELECT * FROM medicine_receipts ORDER BY purchaseDate DESC, createdAt DESC")
    fun observeAll(): Flow<List<MedicineReceiptEntity>>

    @Query("SELECT * FROM medicine_receipts WHERE id = :id")
    suspend fun get(id: String): MedicineReceiptEntity?

    @Query("SELECT * FROM medicine_receipts") suspend fun getAll(): List<MedicineReceiptEntity>
}
