package io.github.max_schall.appiary.data.repository

import android.content.Context
import androidx.core.net.toUri
import io.github.max_schall.appiary.data.dao.PhotoDao
import io.github.max_schall.appiary.data.entity.PhotoAttachmentEntity
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.Flow
import java.io.File

/** Stores inspection/hive photos as JPEGs in app-internal storage (local-first). */
class PhotoRepository(
    private val dao: PhotoDao,
    private val context: Context,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val dir: File by lazy { File(context.filesDir, "photos").apply { mkdirs() } }

    fun observeByHive(hiveId: String): Flow<List<PhotoAttachmentEntity>> = dao.observeByHive(hiveId)

    /** A fresh destination file for CameraX to write into. */
    fun newPhotoFile(): File = File(dir, "${newId()}.jpg")

    suspend fun attach(hiveId: String?, inspectionId: String?, file: File, caption: String? = null): String {
        val now = clock()
        val entity = PhotoAttachmentEntity(
            id = newId(), uri = file.toUri().toString(), hiveId = hiveId, inspectionId = inspectionId,
            caption = caption, takenAt = now, createdAt = now,
        )
        dao.upsert(entity)
        return entity.id
    }

    suspend fun delete(photo: PhotoAttachmentEntity) {
        runCatching { photo.uri.toUri().path?.let { File(it).delete() } }
        dao.delete(photo)
    }
}
