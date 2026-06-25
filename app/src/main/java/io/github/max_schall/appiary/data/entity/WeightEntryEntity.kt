package io.github.max_schall.appiary.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.max_schall.appiary.util.newId
import kotlinx.serialization.Serializable

/**
 * A hive-weight reading (kg) at a point in time — the classic scale series many
 * beekeepers track to infer flow, consumption, and overwintering. `apiaryId` is
 * denormalized for fast filtering, mirroring the other event entities.
 */
@Entity(
    tableName = "weight_entries",
    foreignKeys = [
        ForeignKey(entity = HiveEntity::class, parentColumns = ["id"], childColumns = ["hiveId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("hiveId"), Index("apiaryId"), Index("recordedAt")],
)
@Serializable
data class WeightEntryEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String,
    val apiaryId: String,
    val recordedAt: Long,
    val weightKg: Double,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
