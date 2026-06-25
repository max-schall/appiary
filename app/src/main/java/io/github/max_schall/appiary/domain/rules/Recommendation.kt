package io.github.max_schall.appiary.domain.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket

/**
 * The engine's output unit — a single explainable recommendation. Pure domain
 * value (no persistence identity or user status); the reconciler maps it onto a
 * [io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity] for storage.
 */
data class Recommendation(
    val hiveId: String?,
    val apiaryId: String?,
    val category: RecommendationCategory,
    val urgencyBucket: UrgencyBucket,
    val urgencyScore: Int,
    val title: String,
    val shortReason: String,
    val longExplanation: String,
    val dueAt: Long?,
    val ruleKey: String,
    val actionType: ActionType,
)
