package io.github.max_schall.appiary.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.max_schall.appiary.domain.model.ColonyEventType
import io.github.max_schall.appiary.util.newId
import kotlinx.serialization.Serializable

/**
 * A structural colony operation (split, swarm capture, merge). [hiveId] is the
 * subject — the daughter for a split, the new colony for a capture, the surviving
 * colony for a merge — and [relatedHiveId] is the other party (the parent / the
 * absorbed hive), kept as a loose reference so it survives the related hive being
 * archived. Deleting the subject hive cascades the record away.
 */
@Entity(
    tableName = "colony_events",
    foreignKeys = [
        ForeignKey(entity = HiveEntity::class, parentColumns = ["id"], childColumns = ["hiveId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("hiveId"), Index("relatedHiveId"), Index("occurredAt")],
)
@Serializable
data class ColonyEventEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String,
    val apiaryId: String,
    val type: ColonyEventType,
    val relatedHiveId: String? = null,
    /** Cached display name of the related hive at the time, so history reads well. */
    val relatedHiveName: String? = null,
    val occurredAt: Long,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
