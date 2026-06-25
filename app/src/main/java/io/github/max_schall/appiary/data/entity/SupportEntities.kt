package io.github.max_schall.appiary.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.model.ReminderType
import io.github.max_schall.appiary.util.newId
import kotlinx.serialization.Serializable

/** A captured photo, attachable to a hive and/or a specific inspection. */
@Entity(
    tableName = "photo_attachments",
    foreignKeys = [
        ForeignKey(entity = HiveEntity::class, parentColumns = ["id"], childColumns = ["hiveId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = InspectionEntity::class, parentColumns = ["id"], childColumns = ["inspectionId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("hiveId"), Index("inspectionId")],
)
@Serializable
data class PhotoAttachmentEntity(
    @PrimaryKey val id: String = newId(),
    val uri: String,
    val hiveId: String? = null,
    val inspectionId: String? = null,
    val caption: String? = null,
    val takenAt: Long,
    val widthPx: Int? = null,
    val heightPx: Int? = null,
    val createdAt: Long,
)

/** Reminder configuration row (a small fixed set of these). */
@Entity(tableName = "reminder_settings")
@Serializable
data class ReminderSettingEntity(
    @PrimaryKey val id: String = newId(),
    val type: ReminderType,
    val enabled: Boolean = false,
    val hourOfDay: Int = 8,
    val minute: Int = 0,
    val quietHoursStart: Int? = null,
    val quietHoursEnd: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * Seasonal context that scales rule behavior (inspection cadence, harvest prep,
 * winter feeding windows). A single profile is marked selected at a time.
 */
@Entity(tableName = "seasonal_profiles")
@Serializable
data class SeasonalProfileEntity(
    @PrimaryKey val id: String = newId(),
    val name: String,
    val hemisphere: Hemisphere = Hemisphere.NORTHERN,
    /** Months (1-12) that bound the active beekeeping season. */
    val activeSeasonStartMonth: Int = 3,
    val activeSeasonEndMonth: Int = 9,
    val harvestStartMonth: Int = 7,
    val harvestEndMonth: Int = 9,
    val winterPrepMonth: Int = 9,
    /** Optional location the season was derived from (for display + re-derivation). */
    val latitude: Double? = null,
    val longitude: Double? = null,
    val selected: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
