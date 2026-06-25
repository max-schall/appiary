package io.github.max_schall.appiary.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.max_schall.appiary.domain.model.BroodPattern
import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.DiseaseConcern
import io.github.max_schall.appiary.domain.model.FeedType
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.HarvestProduct
import io.github.max_schall.appiary.domain.model.MiteCheckMethod
import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.model.PestObservation
import io.github.max_schall.appiary.domain.model.QueenEventType
import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.Temperament
import io.github.max_schall.appiary.domain.model.TreatmentType
import io.github.max_schall.appiary.domain.model.YesNoUnsure
import io.github.max_schall.appiary.util.newId
import kotlinx.serialization.Serializable

/** A structured field inspection. `apiaryId` is denormalized for fast filtering. */
@Entity(
    tableName = "inspections",
    foreignKeys = [
        ForeignKey(entity = HiveEntity::class, parentColumns = ["id"], childColumns = ["hiveId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("hiveId"), Index("apiaryId"), Index("performedAt")],
)
@Serializable
data class InspectionEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String,
    val apiaryId: String,
    val performedAt: Long,
    val queenSeen: YesNoUnsure = YesNoUnsure.UNSURE,
    val eggsSeen: YesNoUnsure = YesNoUnsure.UNSURE,
    val broodPattern: BroodPattern = BroodPattern.UNKNOWN,
    val strength: ColonyStrength = ColonyStrength.UNKNOWN,
    val temperament: Temperament = Temperament.UNKNOWN,
    val foodStores: FoodStores = FoodStores.UNKNOWN,
    val swarmSigns: Boolean = false,
    val queenCells: Boolean = false,
    val diseaseConcern: DiseaseConcern = DiseaseConcern.NONE,
    val pests: PestObservation = PestObservation.NONE,
    val notes: String? = null,
    val followUpTaskId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "queen_records",
    foreignKeys = [
        ForeignKey(entity = HiveEntity::class, parentColumns = ["id"], childColumns = ["hiveId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("hiveId"), Index("recordedAt")],
)
@Serializable
data class QueenRecordEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String,
    val recordedAt: Long,
    val event: QueenEventType,
    val resultingStatus: QueenStatus,
    val markColor: String? = null,
    val origin: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "mite_checks",
    foreignKeys = [
        ForeignKey(entity = HiveEntity::class, parentColumns = ["id"], childColumns = ["hiveId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("hiveId"), Index("apiaryId"), Index("checkedAt")],
)
@Serializable
data class MiteCheckEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String,
    val apiaryId: String,
    val checkedAt: Long,
    val method: MiteCheckMethod,
    val sampleSize: Int? = null,
    val mitesCounted: Int? = null,
    /** Mites per 100 bees (infestation %), computed when sample size is known. */
    val mitesPerHundred: Double? = null,
    val result: MiteResult? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "treatment_events",
    foreignKeys = [
        ForeignKey(entity = HiveEntity::class, parentColumns = ["id"], childColumns = ["hiveId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("hiveId"), Index("apiaryId"), Index("startedAt")],
)
@Serializable
data class TreatmentEventEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String,
    val apiaryId: String,
    val type: TreatmentType,
    val startedAt: Long,
    val endedAt: Long? = null,
    val dosage: String? = null,
    val followUpCheckDueAt: Long? = null,
    val notes: String? = null,
    // German Bestandsbuch (EU 2019/6 Art. 108): proof-of-purchase + extra fields.
    val receiptId: String? = null,
    val productName: String? = null,
    val withdrawalDays: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "feeding_events",
    foreignKeys = [
        ForeignKey(entity = HiveEntity::class, parentColumns = ["id"], childColumns = ["hiveId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("hiveId"), Index("apiaryId"), Index("fedAt")],
)
@Serializable
data class FeedingEventEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String,
    val apiaryId: String,
    val fedAt: Long,
    val feedType: FeedType,
    val quantity: Double? = null,
    val unit: String? = null,
    val notes: String? = null,
    val reminderAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/** Harvest can be recorded per-hive (hiveId set) or apiary-wide (hiveId null). */
@Entity(
    tableName = "harvest_events",
    foreignKeys = [
        ForeignKey(entity = HiveEntity::class, parentColumns = ["id"], childColumns = ["hiveId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("hiveId"), Index("apiaryId"), Index("harvestedAt")],
)
@Serializable
data class HarvestEventEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String? = null,
    val apiaryId: String,
    val harvestedAt: Long,
    val product: HarvestProduct = HarvestProduct.HONEY,
    val amountKg: Double? = null,
    val batchNote: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
