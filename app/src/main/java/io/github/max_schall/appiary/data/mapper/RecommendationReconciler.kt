package io.github.max_schall.appiary.data.mapper

import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.domain.model.RecommendationStatus
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.util.newId

/** What the engine run should persist: rows to write, and stale rows to drop. */
data class Reconciliation(
    val toUpsert: List<GeneratedRecommendationEntity>,
    val toDeleteIds: List<String>,
)

/**
 * Pure merge of a fresh engine run with the persisted recommendations. Identity
 * is (hiveId, ruleKey) — the manual rule bakes the task id into its key, so each
 * source maps to a stable row. User state is respected:
 *  - ACTIVE rows are refreshed in place (content updated, id kept).
 *  - SNOOZED rows keep their snooze until it expires, then reactivate.
 *  - DISMISSED / COMPLETED rows are left untouched (won't resurface).
 *  - ACTIVE rows with no matching fresh rec are deleted (condition resolved);
 *    user-held rows are retained.
 */
object RecommendationReconciler {

    private fun matchKey(hiveId: String?, ruleKey: String) = "${hiveId.orEmpty()}|$ruleKey"

    fun reconcile(
        existing: List<GeneratedRecommendationEntity>,
        fresh: List<Recommendation>,
        now: Long,
        idFactory: () -> String = ::newId,
    ): Reconciliation {
        val byKey = existing.associateBy { matchKey(it.hiveId, it.generatedFromRuleKey) }
        val handledKeys = mutableSetOf<String>()
        val upserts = mutableListOf<GeneratedRecommendationEntity>()

        for (rec in fresh) {
            val key = matchKey(rec.hiveId, rec.ruleKey)
            handledKeys += key
            val prior = byKey[key]

            when (prior?.status) {
                null -> upserts += rec.toNewEntity(idFactory(), now)

                RecommendationStatus.ACTIVE ->
                    upserts += prior.withContentFrom(rec, now)

                RecommendationStatus.SNOOZED -> {
                    val expired = prior.snoozedUntil == null || prior.snoozedUntil <= now
                    upserts += if (expired) {
                        prior.withContentFrom(rec, now)
                            .copy(status = RecommendationStatus.ACTIVE, snoozedUntil = null)
                    } else {
                        prior.withContentFrom(rec, now) // keep snooze + status
                    }
                }

                // Dismissed / completed: respect the user's decision, leave as-is.
                RecommendationStatus.DISMISSED, RecommendationStatus.COMPLETED -> Unit
            }
        }

        // Active rows whose condition no longer holds are removed; user-held rows stay.
        val toDelete = existing
            .filter { it.status == RecommendationStatus.ACTIVE }
            .filterNot { matchKey(it.hiveId, it.generatedFromRuleKey) in handledKeys }
            .map { it.id }

        return Reconciliation(upserts, toDelete)
    }

    private fun Recommendation.toNewEntity(id: String, now: Long) = GeneratedRecommendationEntity(
        id = id,
        hiveId = hiveId,
        apiaryId = apiaryId,
        category = category,
        urgencyBucket = urgencyBucket,
        urgencyScore = urgencyScore,
        title = title,
        shortReason = shortReason,
        longExplanation = longExplanation,
        dueAt = dueAt,
        generatedFromRuleKey = ruleKey,
        recommendedActionType = actionType,
        status = RecommendationStatus.ACTIVE,
        snoozedUntil = null,
        createdAt = now,
        updatedAt = now,
    )

    private fun GeneratedRecommendationEntity.withContentFrom(rec: Recommendation, now: Long) = copy(
        apiaryId = rec.apiaryId,
        category = rec.category,
        urgencyBucket = rec.urgencyBucket,
        urgencyScore = rec.urgencyScore,
        title = rec.title,
        shortReason = rec.shortReason,
        longExplanation = rec.longExplanation,
        dueAt = rec.dueAt,
        recommendedActionType = rec.actionType,
        updatedAt = now,
    )
}
