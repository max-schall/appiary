package io.github.max_schall.appiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.max_schall.appiary.data.entity.PhotoAttachmentEntity
import io.github.max_schall.appiary.data.entity.ReminderSettingEntity
import io.github.max_schall.appiary.data.entity.SeasonalProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Upsert suspend fun upsert(photo: PhotoAttachmentEntity)
    @Delete suspend fun delete(photo: PhotoAttachmentEntity)

    @Query("SELECT * FROM photo_attachments WHERE hiveId = :hiveId ORDER BY takenAt DESC")
    fun observeByHive(hiveId: String): Flow<List<PhotoAttachmentEntity>>

    @Query("SELECT * FROM photo_attachments WHERE inspectionId = :inspectionId ORDER BY takenAt DESC")
    fun observeByInspection(inspectionId: String): Flow<List<PhotoAttachmentEntity>>

    @Query("SELECT * FROM photo_attachments") suspend fun getAll(): List<PhotoAttachmentEntity>
}

@Dao
interface ReminderDao {
    @Upsert suspend fun upsert(setting: ReminderSettingEntity)
    @Upsert suspend fun upsertAll(items: List<ReminderSettingEntity>)

    @Query("SELECT * FROM reminder_settings")
    fun observeAll(): Flow<List<ReminderSettingEntity>>

    @Query("SELECT * FROM reminder_settings")
    suspend fun getAll(): List<ReminderSettingEntity>
}

@Dao
interface SeasonalProfileDao {
    @Upsert suspend fun upsert(profile: SeasonalProfileEntity)
    @Upsert suspend fun upsertAll(items: List<SeasonalProfileEntity>)

    @Query("SELECT * FROM seasonal_profiles ORDER BY name")
    fun observeAll(): Flow<List<SeasonalProfileEntity>>

    @Query("SELECT * FROM seasonal_profiles WHERE selected = 1 LIMIT 1")
    fun observeSelected(): Flow<SeasonalProfileEntity?>

    @Query("SELECT * FROM seasonal_profiles WHERE selected = 1 LIMIT 1")
    suspend fun getSelected(): SeasonalProfileEntity?

    @Query("SELECT COUNT(*) FROM seasonal_profiles")
    suspend fun count(): Int

    @Query("SELECT * FROM seasonal_profiles") suspend fun getAll(): List<SeasonalProfileEntity>
}
