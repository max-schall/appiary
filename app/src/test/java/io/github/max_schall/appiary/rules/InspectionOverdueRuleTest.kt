package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.InspectionOverdueRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InspectionOverdueRuleTest {
    private val config = RuleConfig.DEFAULT

    @Test fun `not due when within interval and outside warning window`() {
        val ctx = Fixtures.ctx(Fixtures.hive(lastInspectionAt = Fixtures.ago(5)))
        assertTrue(InspectionOverdueRule.evaluate(ctx, config).isEmpty())
    }

    @Test fun `due soon when approaching the interval`() {
        val ctx = Fixtures.ctx(Fixtures.hive(lastInspectionAt = Fixtures.ago(12)))
        val recs = InspectionOverdueRule.evaluate(ctx, config)
        assertEquals(1, recs.size)
        assertEquals(UrgencyBucket.DUE_SOON, recs.first().urgencyBucket)
    }

    @Test fun `do now when overdue, explanation cites days and interval`() {
        val ctx = Fixtures.ctx(Fixtures.hive(lastInspectionAt = Fixtures.ago(20)))
        val rec = InspectionOverdueRule.evaluate(ctx, config).single()
        assertEquals(UrgencyBucket.DO_NOW, rec.urgencyBucket)
        assertTrue(rec.longExplanation.contains("20 days ago"))
        assertTrue(rec.longExplanation.contains("14 days")) // the interval
        assertTrue(rec.shortReason.contains("Overdue"))
    }

    @Test fun `never inspected uses install date and escalates`() {
        val hive = Fixtures.hive(lastInspectionAt = null, installedAt = Fixtures.ago(20))
        val rec = InspectionOverdueRule.evaluate(Fixtures.ctx(hive), config).single()
        assertEquals(UrgencyBucket.DO_NOW, rec.urgencyBucket)
        assertTrue(rec.longExplanation.contains("set up"))
    }

    @Test fun `mentions unconfirmed queen when relevant`() {
        val hive = Fixtures.hive(lastInspectionAt = Fixtures.ago(20), queenStatus = QueenStatus.UNCERTAIN)
        val rec = InspectionOverdueRule.evaluate(Fixtures.ctx(hive), config).single()
        assertTrue(rec.longExplanation.contains("queen status is also unconfirmed"))
    }

    @Test fun `off-season uses the longer interval`() {
        // 20 days since inspection is overdue in-season but fine off-season (30d).
        val ctx = Fixtures.ctx(Fixtures.hive(lastInspectionAt = Fixtures.ago(20)), month = 1)
        assertTrue(InspectionOverdueRule.evaluate(ctx, config).isEmpty())
    }
}
