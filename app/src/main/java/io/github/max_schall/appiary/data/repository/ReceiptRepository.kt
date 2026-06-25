package io.github.max_schall.appiary.data.repository

import android.content.Context
import androidx.core.net.toUri
import io.github.max_schall.appiary.data.dao.MedicineReceiptDao
import io.github.max_schall.appiary.data.entity.MedicineReceiptEntity
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.Flow
import java.io.File

/** Stores Bestandsbuch proof-of-purchase receipts (photo in app-internal storage). */
class ReceiptRepository(
    private val dao: MedicineReceiptDao,
    private val context: Context,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val dir: File by lazy { File(context.filesDir, "receipts").apply { mkdirs() } }

    fun observeAll(): Flow<List<MedicineReceiptEntity>> = dao.observeAll()
    suspend fun get(id: String): MedicineReceiptEntity? = dao.get(id)

    /** A fresh destination file for CameraX to write the receipt photo into. */
    fun newPhotoFile(): File = File(dir, "${newId()}.jpg")

    /** Create a receipt from a captured photo (supplier details filled in later). */
    suspend fun createFromPhoto(file: File): String {
        val now = clock()
        val receipt = MedicineReceiptEntity(
            id = newId(), photoUri = file.toUri().toString(), createdAt = now, updatedAt = now,
        )
        dao.upsert(receipt)
        return receipt.id
    }

    suspend fun save(receipt: MedicineReceiptEntity) = dao.upsert(receipt.copy(updatedAt = clock()))

    suspend fun delete(receipt: MedicineReceiptEntity) {
        runCatching { receipt.photoUri?.toUri()?.path?.let { File(it).delete() } }
        dao.delete(receipt)
    }
}
