package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.model.YesNoUnsure
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.QueenUncertaintyRule
import io.github.max_schall.appiary.domain.rules.rules.RepeatedQueenUncertaintyRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QueenRulesTest {
    private val config = RuleConfig.DEFAULT

    private fun unconfirmed(id: String, daysAgo: Long) = Fixtures.inspection(
        id = id, daysAgo = daysAgo, queenSeen = YesNoUnsure.NO, eggsSeen = YesNoUnsure.UNSURE,
    )

    @Test fun `queenright produces nothing`() {
        val ctx = Fixtures.ctx(Fixtures.hive(queenStatus = QueenStatus.QUEENRIGHT))
        assertTrue(QueenUncertaintyRule.evaluate(ctx, config).isEmpty())
        assertTrue(RepeatedQueenUncertaintyRule.evaluate(ctx, config).isEmpty())
    }

    @Test fun `recent uncertainty is due soon`() {
        val hive = Fixtures.hive(queenStatus = QueenStatus.UNCERTAIN, lastInspectionAt = Fixtures.ago(2))
        val ctx = Fixtures.ctx(hive, inspections = listOf(unconfirmed("i1", 2)))
        val rec = QueenUncertaintyRule.evaluate(ctx, config).single()
        assertEquals(UrgencyBucket.DUE_SOON, rec.urgencyBucket)
        assertTrue(rec.longExplanation.contains("not confirmed"))
    }

    @Test fun `stale uncertainty escalates to do now`() {
        val hive = Fixtures.hive(queenStatus = QueenStatus.UNCERTAIN, lastInspectionAt = Fixtures.ago(9))
        val ctx = Fixtures.ctx(hive, inspections = listOf(unconfirmed("i1", 9)))
        assertEquals(UrgencyBucket.DO_NOW, QueenUncertaintyRule.evaluate(ctx, config).single().urgencyBucket)
    }

    @Test fun `queenless is do now`() {
        val hive = Fixtures.hive(queenStatus = QueenStatus.QUEENLESS, lastInspectionAt = Fixtures.ago(1))
        val ctx = Fixtures.ctx(hive, inspections = listOf(unconfirmed("i1", 1)))
        val rec = QueenUncertaintyRule.evaluate(ctx, config).single()
        assertEquals(UrgencyBucket.DO_NOW, rec.urgencyBucket)
        assertTrue(rec.longExplanation.contains("queenless"))
    }

    @Test fun `repeated uncertainty defers single rule and escalates`() {
        val hive = Fixtures.hive(queenStatus = QueenStatus.UNCERTAIN, lastInspectionAt = Fixtures.ago(3))
        val ctx = Fixtures.ctx(
            hive,
            inspections = listOf(unconfirmed("i1", 3), unconfirmed("i2", 17)),
        )
        // Single-visit rule steps aside...
        assertTrue(QueenUncertaintyRule.evaluate(ctx, config).isEmpty())
        // ...and the escalation rule fires.
        val rec = RepeatedQueenUncertaintyRule.evaluate(ctx, config).single()
        assertEquals(UrgencyBucket.DO_NOW, rec.urgencyBucket)
        assertTrue(rec.shortReason.contains("2 visits"))
        assertTrue(rec.longExplanation.contains("requeen"))
    }

    @Test fun `single unconfirmed visit does not trigger escalation`() {
        val hive = Fixtures.hive(queenStatus = QueenStatus.UNCERTAIN)
        val ctx = Fixtures.ctx(hive, inspections = listOf(unconfirmed("i1", 3)))
        assertTrue(RepeatedQueenUncertaintyRule.evaluate(ctx, config).isEmpty())
    }
}
