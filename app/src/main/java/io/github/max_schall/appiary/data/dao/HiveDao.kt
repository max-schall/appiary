package io.github.max_schall.appiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.HiveStatusSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveDao {

    @Upsert suspend fun upsert(hive: HiveEntity)
    @Upsert suspend fun upsertAll(hives: List<HiveEntity>)
    @Delete suspend fun delete(hive: HiveEntity)

    @Query("SELECT * FROM hives WHERE archived = 0 ORDER BY name")
    fun observeAll(): Flow<List<HiveEntity>>

    @Query("SELECT * FROM hives WHERE apiaryId = :apiaryId AND archived = 0 ORDER BY name")
    fun observeByApiary(apiaryId: String): Flow<List<HiveEntity>>

    @Query("SELECT * FROM hives WHERE id = :id")
    fun observeHive(id: String): Flow<HiveEntity?>

    @Query("SELECT * FROM hives WHERE id = :id")
    suspend fun getHive(id: String): HiveEntity?

    @Query("SELECT * FROM hives WHERE nfcTagId = :tagId LIMIT 1")
    suspend fun getByNfcTag(tagId: String): HiveEntity?

    /** Non-archived hives — the working set fed to the rules engine. */
    @Query("SELECT * FROM hives WHERE archived = 0")
    suspend fun getActiveHives(): List<HiveEntity>

    @Query("UPDATE hives SET archived = :archived, updatedAt = :now WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, now: Long)

    // --- Status snapshots ---------------------------------------------------
    @Upsert suspend fun upsertSnapshot(snapshot: HiveStatusSnapshotEntity)
    @Upsert suspend fun upsertSnapshots(snapshots: List<HiveStatusSnapshotEntity>)

    @Query("SELECT * FROM hive_status_snapshots WHERE hiveId = :hiveId ORDER BY takenAt DESC")
    fun observeSnapshots(hiveId: String): Flow<List<HiveStatusSnapshotEntity>>

    @Query("SELECT * FROM hives") suspend fun getAllHives(): List<HiveEntity>
    @Query("SELECT * FROM hive_status_snapshots") suspend fun getAllSnapshots(): List<HiveStatusSnapshotEntity>
}
