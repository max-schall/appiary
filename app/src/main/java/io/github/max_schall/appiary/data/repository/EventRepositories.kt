package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.FeedingDao
import io.github.max_schall.appiary.data.dao.HarvestDao
import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.dao.MiteCheckDao
import io.github.max_schall.appiary.data.dao.QueenRecordDao
import io.github.max_schall.appiary.data.dao.TreatmentDao
import io.github.max_schall.appiary.data.entity.FeedingEventEntity
import io.github.max_schall.appiary.data.entity.HarvestEventEntity
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.data.entity.QueenRecordEntity
import io.github.max_schall.appiary.data.entity.TreatmentEventEntity
import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.model.TreatmentState
import kotlinx.coroutines.flow.Flow

class MiteCheckRepository(
    private val dao: MiteCheckDao,
    private val hiveDao: HiveDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeByHive(hiveId: String): Flow<List<MiteCheckEntity>> = dao.observeByHive(hiveId)
    suspend fun latestForHive(hiveId: String): MiteCheckEntity? = dao.latestForHive(hiveId)

    suspend fun save(check: MiteCheckEntity) {
        val now = clock()
        // Compute infestation % and a default interpretation when raw counts exist.
        val perHundred = check.mitesPerHundred
            ?: if (check.sampleSize != null && check.sampleSize > 0 && check.mitesCounted != null) {
                check.mitesCounted * 100.0 / check.sampleSize
            } else null
        val result = check.result ?: perHundred?.let(::interpret)

        dao.upsert(check.copy(mitesPerHundred = perHundred, result = result, updatedAt = now))

        hiveDao.getHive(check.hiveId)?.let { hive ->
            val clearsFollowUp = hive.postTreatmentCheckDueAt != null
            hiveDao.upsert(
                hive.copy(
                    lastMiteCheckAt = check.checkedAt,
                    postTreatmentCheckDueAt = if (clearsFollowUp) null else hive.postTreatmentCheckDueAt,
                    treatmentState = if (hive.treatmentState == TreatmentState.FOLLOW_UP_DUE) {
                        TreatmentState.COMPLETED
                    } else hive.treatmentState,
                    updatedAt = now,
                ),
            )
        }
    }

    companion object {
        /** Default alcohol-wash interpretation (% mites per 100 bees). Phase 3
         *  makes the thresholds configurable via RuleConfig. */
        fun interpret(perHundred: Double): MiteResult = when {
            perHundred < 1.0 -> MiteResult.LOW
            perHundred < 3.0 -> MiteResult.MODERATE
            perHundred < 5.0 -> MiteResult.HIGH
            else -> MiteResult.CRITICAL
        }
    }
}

class TreatmentRepository(
    private val dao: TreatmentDao,
    private val hiveDao: HiveDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeByHive(hiveId: String): Flow<List<TreatmentEventEntity>> = dao.observeByHive(hiveId)
    fun observeByApiary(apiaryId: String): Flow<List<TreatmentEventEntity>> = dao.observeByApiary(apiaryId)
    suspend fun getByApiary(apiaryId: String): List<TreatmentEventEntity> = dao.getByApiary(apiaryId)
    suspend fun latestForHive(hiveId: String): TreatmentEventEntity? = dao.latestForHive(hiveId)

    /** Link (or clear) a proof-of-purchase receipt on a treatment. */
    suspend fun linkReceipt(treatmentId: String, receiptId: String?) {
        dao.getById(treatmentId)?.let { dao.upsert(it.copy(receiptId = receiptId, updatedAt = clock())) }
    }

    /** Update the Bestandsbuch-specific fields (medicine name, withdrawal period). */
    suspend fun updateRecordDetails(treatmentId: String, productName: String?, withdrawalDays: Int?) {
        dao.getById(treatmentId)?.let {
            dao.upsert(it.copy(productName = productName, withdrawalDays = withdrawalDays, updatedAt = clock()))
        }
    }

    suspend fun save(event: TreatmentEventEntity) {
        val now = clock()
        dao.upsert(event.copy(updatedAt = now))

        hiveDao.getHive(event.hiveId)?.let { hive ->
            val state = when {
                event.endedAt == null -> TreatmentState.IN_PROGRESS
                event.followUpCheckDueAt != null -> TreatmentState.FOLLOW_UP_DUE
                else -> TreatmentState.COMPLETED
            }
            hiveDao.upsert(
                hive.copy(
                    treatmentState = state,
                    lastTreatmentEndedAt = event.endedAt ?: hive.lastTreatmentEndedAt,
                    postTreatmentCheckDueAt = event.followUpCheckDueAt,
                    updatedAt = now,
                ),
            )
        }
    }
}

class FeedingRepository(
    private val dao: FeedingDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeByHive(hiveId: String): Flow<List<FeedingEventEntity>> = dao.observeByHive(hiveId)
    suspend fun save(event: FeedingEventEntity) = dao.upsert(event.copy(updatedAt = clock()))
}

class HarvestRepository(
    private val dao: HarvestDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeByHive(hiveId: String): Flow<List<HarvestEventEntity>> = dao.observeByHive(hiveId)
    fun observeByApiary(apiaryId: String): Flow<List<HarvestEventEntity>> = dao.observeByApiary(apiaryId)
    suspend fun save(event: HarvestEventEntity) = dao.upsert(event.copy(updatedAt = clock()))
}

class QueenRecordRepository(
    private val dao: QueenRecordDao,
    private val hiveDao: HiveDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeByHive(hiveId: String): Flow<List<QueenRecordEntity>> = dao.observeByHive(hiveId)

    suspend fun save(record: QueenRecordEntity) {
        val now = clock()
        dao.upsert(record.copy(updatedAt = now))
        hiveDao.getHive(record.hiveId)?.let { hive ->
            hiveDao.upsert(hive.copy(queenStatus = record.resultingStatus, updatedAt = now))
        }
    }
}
