package io.github.max_schall.appiary.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.max_schall.appiary.domain.model.BroodPattern
import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.HiveStatus
import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.Temperament
import io.github.max_schall.appiary.domain.model.TreatmentState
import io.github.max_schall.appiary.util.newId
import kotlinx.serialization.Serializable

/** A physical location where apiaries may sit (optional geo metadata). */
@Serializable
@Entity(tableName = "apiary_sites")
data class ApiarySiteEntity(
    @PrimaryKey val id: String = newId(),
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    /** ISO 3166-1 alpha-2 country (e.g. "DE"), resolved from coordinates. */
    val countryCode: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/** A managed yard of hives. The primary organizational unit in the UI. */
@Entity(
    tableName = "apiaries",
    foreignKeys = [
        ForeignKey(
            entity = ApiarySiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("siteId")],
)
@Serializable
data class ApiaryEntity(
    @PrimaryKey val id: String = newId(),
    val name: String,
    val siteId: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * A colony. Current-state fields (status/queen/brood/etc.) and the last-event
 * timestamps are denormalized here so lists and the rules engine stay fast; the
 * repository keeps them in sync whenever an inspection/mite check/treatment is
 * logged. [HiveStatusSnapshotEntity] preserves the historical state log.
 */
@Entity(
    tableName = "hives",
    foreignKeys = [
        ForeignKey(
            entity = ApiaryEntity::class,
            parentColumns = ["id"],
            childColumns = ["apiaryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("apiaryId")],
)
@Serializable
data class HiveEntity(
    @PrimaryKey val id: String = newId(),
    val apiaryId: String,
    val name: String,
    val status: HiveStatus = HiveStatus.ACTIVE,
    val queenStatus: QueenStatus = QueenStatus.UNCERTAIN,
    val broodPattern: BroodPattern = BroodPattern.UNKNOWN,
    val strength: ColonyStrength = ColonyStrength.UNKNOWN,
    val temperament: Temperament = Temperament.UNKNOWN,
    val foodStores: FoodStores = FoodStores.UNKNOWN,
    val treatmentState: TreatmentState = TreatmentState.NONE,
    val lastInspectionAt: Long? = null,
    val lastMiteCheckAt: Long? = null,
    val lastTreatmentEndedAt: Long? = null,
    val postTreatmentCheckDueAt: Long? = null,
    val installedAt: Long? = null,
    val nfcTagId: String? = null,
    val notes: String? = null,
    val archived: Boolean = false,
    /** Lineage: how this colony started and (for splits) which hive it came from. */
    val originType: io.github.max_schall.appiary.domain.model.HiveOrigin =
        io.github.max_schall.appiary.domain.model.HiveOrigin.UNKNOWN,
    val parentHiveId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/** Point-in-time snapshot of a hive's structured state (one per inspection). */
@Entity(
    tableName = "hive_status_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = HiveEntity::class,
            parentColumns = ["id"],
            childColumns = ["hiveId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("hiveId")],
)
@Serializable
data class HiveStatusSnapshotEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String,
    val takenAt: Long,
    val status: HiveStatus,
    val queenStatus: QueenStatus,
    val broodPattern: BroodPattern,
    val strength: ColonyStrength,
    val temperament: Temperament,
    val foodStores: FoodStores,
    /** Provenance, e.g. "inspection:<id>" or "manual". */
    val source: String,
    val createdAt: Long,
)
