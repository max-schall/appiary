package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.data.mapper.RecommendationReconciler
import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.RecommendationStatus
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.Recommendation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationReconcilerTest {

    private val now = Fixtures.NOW

    private fun fresh(ruleKey: String = "inspection_overdue", hiveId: String? = "h1", title: String = "Inspect H1") =
        Recommendation(
            hiveId = hiveId, apiaryId = "a1", category = RecommendationCategory.INSPECTION,
            urgencyBucket = UrgencyBucket.DO_NOW, urgencyScore = 80, title = title,
            shortReason = "Overdue", longExplanation = "…", dueAt = now,
            ruleKey = ruleKey, actionType = ActionType.LOG_INSPECTION,
        )

    private fun existing(
        id: String = "r1", ruleKey: String = "inspection_overdue", hiveId: String? = "h1",
        status: RecommendationStatus = RecommendationStatus.ACTIVE, snoozedUntil: Long? = null,
        title: String = "old title",
    ) = GeneratedRecommendationEntity(
        id = id, hiveId = hiveId, apiaryId = "a1", category = RecommendationCategory.INSPECTION,
        urgencyBucket = UrgencyBucket.DUE_SOON, urgencyScore = 40, title = title, shortReason = "old",
        longExplanation = "old", dueAt = null, generatedFromRuleKey = ruleKey,
        recommendedActionType = ActionType.LOG_INSPECTION, status = status, snoozedUntil = snoozedUntil,
        createdAt = now - 1000, updatedAt = now - 1000,
    )

    @Test fun `new recommendation is inserted active`() {
        val r = RecommendationReconciler.reconcile(emptyList(), listOf(fresh()), now) { "new-id" }
        assertEquals(1, r.toUpsert.size)
        assertEquals(RecommendationStatus.ACTIVE, r.toUpsert.single().status)
        assertEquals("new-id", r.toUpsert.single().id)
        assertTrue(r.toDeleteIds.isEmpty())
    }

    @Test fun `active match keeps id but refreshes content`() {
        val r = RecommendationReconciler.reconcile(listOf(existing()), listOf(fresh(title = "Inspect H1")), now)
        val row = r.toUpsert.single()
        assertEquals("r1", row.id)
        assertEquals("Inspect H1", row.title)
        assertEquals(UrgencyBucket.DO_NOW, row.urgencyBucket)
        assertTrue(r.toDeleteIds.isEmpty())
    }

    @Test fun `active recommendation with no fresh match is deleted`() {
        val r = RecommendationReconciler.reconcile(listOf(existing()), emptyList(), now)
        assertEquals(listOf("r1"), r.toDeleteIds)
        assertTrue(r.toUpsert.isEmpty())
    }

    @Test fun `unexpired snooze is preserved`() {
        val snoozed = existing(status = RecommendationStatus.SNOOZED, snoozedUntil = now + 10_000)
        val row = RecommendationReconciler.reconcile(listOf(snoozed), listOf(fresh()), now).toUpsert.single()
        assertEquals(RecommendationStatus.SNOOZED, row.status)
        assertEquals(now + 10_000, row.snoozedUntil)
        assertEquals("Inspect H1", row.title) // content still refreshed
    }

    @Test fun `expired snooze reactivates`() {
        val snoozed = existing(status = RecommendationStatus.SNOOZED, snoozedUntil = now - 10_000)
        val row = RecommendationReconciler.reconcile(listOf(snoozed), listOf(fresh()), now).toUpsert.single()
        assertEquals(RecommendationStatus.ACTIVE, row.status)
        assertEquals(null, row.snoozedUntil)
    }

    @Test fun `dismissed recommendation is respected and never resurfaces`() {
        val dismissed = existing(status = RecommendationStatus.DISMISSED)
        val r = RecommendationReconciler.reconcile(listOf(dismissed), listOf(fresh()), now)
        assertTrue(r.toUpsert.isEmpty()) // left untouched
        assertTrue(r.toDeleteIds.isEmpty()) // not deleted either
    }

    @Test fun `dismissed unmatched row is kept, not deleted`() {
        val dismissed = existing(status = RecommendationStatus.DISMISSED)
        val r = RecommendationReconciler.reconcile(listOf(dismissed), emptyList(), now)
        assertTrue(r.toDeleteIds.isEmpty())
    }
}
