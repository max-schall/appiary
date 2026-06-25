package io.github.max_schall.appiary.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.RecommendationStatus
import io.github.max_schall.appiary.domain.model.TaskStatus
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.util.newId
import kotlinx.serialization.Serializable

/**
 * User-created follow-up. May be free-floating or attached to a hive/apiary.
 * No FK so a task survives hive deletion (it just loses its link).
 */
@Entity(
    tableName = "manual_tasks",
    indices = [Index("hiveId"), Index("apiaryId"), Index("status")],
)
@Serializable
data class ManualTaskEntity(
    @PrimaryKey val id: String = newId(),
    val title: String,
    val details: String? = null,
    val hiveId: String? = null,
    val apiaryId: String? = null,
    val dueAt: Long? = null,
    val status: TaskStatus = TaskStatus.OPEN,
    val snoozedUntil: Long? = null,
    val completedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * A recommendation produced by the deterministic rules engine and persisted so
 * it can be snoozed/dismissed and surfaced by reminders. Regenerated whenever
 * relevant data changes; user status (snoozed/dismissed) is preserved by
 * matching on (hiveId, generatedFromRuleKey).
 */
@Entity(
    tableName = "recommendations",
    indices = [Index("hiveId"), Index("apiaryId"), Index("status"), Index("generatedFromRuleKey")],
)
@Serializable
data class GeneratedRecommendationEntity(
    @PrimaryKey val id: String = newId(),
    val hiveId: String?,
    val apiaryId: String?,
    val category: RecommendationCategory,
    val urgencyBucket: UrgencyBucket,
    val urgencyScore: Int,
    val title: String,
    val shortReason: String,
    val longExplanation: String,
    val dueAt: Long? = null,
    val generatedFromRuleKey: String,
    val recommendedActionType: ActionType,
    val status: RecommendationStatus = RecommendationStatus.ACTIVE,
    val snoozedUntil: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
