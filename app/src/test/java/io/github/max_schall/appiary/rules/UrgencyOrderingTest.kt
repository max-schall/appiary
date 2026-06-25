package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.domain.rules.RuleEngine
import io.github.max_schall.appiary.domain.rules.rank
import io.github.max_schall.appiary.domain.rules.urgencyScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrgencyOrderingTest {

    @Test fun `bucket ranks order do-now first and healthy last`() {
        val sorted = UrgencyBucket.entries.sortedBy { it.rank }
        assertEquals(
            listOf(UrgencyBucket.DO_NOW, UrgencyBucket.DUE_SOON, UrgencyBucket.WATCHLIST, UrgencyBucket.HEALTHY),
            sorted,
        )
    }

    @Test fun `severity is clamped to its band`() {
        assertEquals(70, urgencyScore(UrgencyBucket.DO_NOW, -5))
        assertEquals(100, urgencyScore(UrgencyBucket.DO_NOW, 99))
    }

    @Test fun `bucket rank wins over score in the engine ordering`() {
        fun rec(bucket: UrgencyBucket, score: Int) = Recommendation(
            hiveId = "h", apiaryId = "a", category = RecommendationCategory.INSPECTION,
            urgencyBucket = bucket, urgencyScore = score, title = "t", shortReason = "",
            longExplanation = "", dueAt = null, ruleKey = "k", actionType = ActionType.REVIEW,
        )
        // A high-scoring Due-soon must still sort after a low-scoring Do-now.
        val doNow = rec(UrgencyBucket.DO_NOW, 70)
        val dueSoon = rec(UrgencyBucket.DUE_SOON, 70)
        val sorted = listOf(dueSoon, doNow).sortedWith(RuleEngine.URGENCY_ORDER)
        assertEquals(listOf(doNow, dueSoon), sorted)
    }
}
