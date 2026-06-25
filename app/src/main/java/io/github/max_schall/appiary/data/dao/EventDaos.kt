package io.github.max_schall.appiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.max_schall.appiary.data.entity.FeedingEventEntity
import io.github.max_schall.appiary.data.entity.HarvestEventEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.data.entity.QueenRecordEntity
import io.github.max_schall.appiary.data.entity.TreatmentEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Upsert suspend fun upsert(inspection: InspectionEntity)
    @Upsert suspend fun upsertAll(items: List<InspectionEntity>)
    @Delete suspend fun delete(inspection: InspectionEntity)

    @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY performedAt DESC")
    fun observeByHive(hiveId: String): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY performedAt DESC LIMIT 1")
    suspend fun latestForHive(hiveId: String): InspectionEntity?

    @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY performedAt DESC LIMIT :limit")
    suspend fun recentForHive(hiveId: String, limit: Int): List<InspectionEntity>

    @Query("SELECT * FROM inspections ORDER BY performedAt")
    fun observeAll(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections") suspend fun getAll(): List<InspectionEntity>
}

@Dao
interface QueenRecordDao {
    @Upsert suspend fun upsert(record: QueenRecordEntity)
    @Upsert suspend fun upsertAll(items: List<QueenRecordEntity>)
    @Delete suspend fun delete(record: QueenRecordEntity)

    @Query("SELECT * FROM queen_records WHERE hiveId = :hiveId ORDER BY recordedAt DESC")
    fun observeByHive(hiveId: String): Flow<List<QueenRecordEntity>>

    @Query("SELECT * FROM queen_records") suspend fun getAll(): List<QueenRecordEntity>
}

@Dao
interface MiteCheckDao {
    @Upsert suspend fun upsert(check: MiteCheckEntity)
    @Upsert suspend fun upsertAll(items: List<MiteCheckEntity>)
    @Delete suspend fun delete(check: MiteCheckEntity)

    @Query("SELECT * FROM mite_checks WHERE hiveId = :hiveId ORDER BY checkedAt DESC")
    fun observeByHive(hiveId: String): Flow<List<MiteCheckEntity>>

    @Query("SELECT * FROM mite_checks WHERE hiveId = :hiveId ORDER BY checkedAt DESC LIMIT 1")
    suspend fun latestForHive(hiveId: String): MiteCheckEntity?

    @Query("SELECT * FROM mite_checks ORDER BY checkedAt")
    fun observeAll(): Flow<List<MiteCheckEntity>>

    @Query("SELECT * FROM mite_checks") suspend fun getAll(): List<MiteCheckEntity>
}

@Dao
interface TreatmentDao {
    @Upsert suspend fun upsert(event: TreatmentEventEntity)
    @Upsert suspend fun upsertAll(items: List<TreatmentEventEntity>)
    @Delete suspend fun delete(event: TreatmentEventEntity)

    @Query("SELECT * FROM treatment_events WHERE hiveId = :hiveId ORDER BY startedAt DESC")
    fun observeByHive(hiveId: String): Flow<List<TreatmentEventEntity>>

    @Query("SELECT * FROM treatment_events WHERE hiveId = :hiveId ORDER BY startedAt DESC LIMIT 1")
    suspend fun latestForHive(hiveId: String): TreatmentEventEntity?

    @Query("SELECT * FROM treatment_events") suspend fun getAll(): List<TreatmentEventEntity>

    @Query("SELECT * FROM treatment_events WHERE apiaryId = :apiaryId ORDER BY startedAt DESC")
    fun observeByApiary(apiaryId: String): Flow<List<TreatmentEventEntity>>

    @Query("SELECT * FROM treatment_events WHERE apiaryId = :apiaryId ORDER BY startedAt")
    suspend fun getByApiary(apiaryId: String): List<TreatmentEventEntity>

    @Query("SELECT * FROM treatment_events WHERE id = :id")
    suspend fun getById(id: String): TreatmentEventEntity?

    /** Treatments at an apiary still lacking a proof-of-purchase receipt. */
    @Query("SELECT COUNT(*) FROM treatment_events WHERE apiaryId = :apiaryId AND receiptId IS NULL")
    suspend fun countMissingReceipt(apiaryId: String): Int
}

@Dao
interface FeedingDao {
    @Upsert suspend fun upsert(event: FeedingEventEntity)
    @Upsert suspend fun upsertAll(items: List<FeedingEventEntity>)
    @Delete suspend fun delete(event: FeedingEventEntity)

    @Query("SELECT * FROM feeding_events WHERE hiveId = :hiveId ORDER BY fedAt DESC")
    fun observeByHive(hiveId: String): Flow<List<FeedingEventEntity>>

    @Query("SELECT * FROM feeding_events") suspend fun getAll(): List<FeedingEventEntity>
}

@Dao
interface HarvestDao {
    @Upsert suspend fun upsert(event: HarvestEventEntity)
    @Upsert suspend fun upsertAll(items: List<HarvestEventEntity>)
    @Delete suspend fun delete(event: HarvestEventEntity)

    @Query("SELECT * FROM harvest_events WHERE hiveId = :hiveId ORDER BY harvestedAt DESC")
    fun observeByHive(hiveId: String): Flow<List<HarvestEventEntity>>

    @Query("SELECT * FROM harvest_events WHERE apiaryId = :apiaryId ORDER BY harvestedAt DESC")
    fun observeByApiary(apiaryId: String): Flow<List<HarvestEventEntity>>

    @Query("SELECT * FROM harvest_events ORDER BY harvestedAt")
    fun observeAll(): Flow<List<HarvestEventEntity>>

    @Query("SELECT * FROM harvest_events") suspend fun getAll(): List<HarvestEventEntity>
}
