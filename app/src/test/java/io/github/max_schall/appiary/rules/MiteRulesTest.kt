package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.model.TreatmentState
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.MiteCheckOverdueRule
import io.github.max_schall.appiary.domain.rules.rules.PostTreatmentCheckRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MiteRulesTest {
    private val config = RuleConfig.DEFAULT

    // --- Rule 5: mite check overdue ---
    @Test fun `recent mite check is fine`() {
        val ctx = Fixtures.ctx(Fixtures.hive(lastMiteCheckAt = Fixtures.ago(10)))
        assertTrue(MiteCheckOverdueRule.evaluate(ctx, config).isEmpty())
    }

    @Test fun `mildly overdue is due soon`() {
        val ctx = Fixtures.ctx(Fixtures.hive(lastMiteCheckAt = Fixtures.ago(25)))
        assertEquals(UrgencyBucket.DUE_SOON, MiteCheckOverdueRule.evaluate(ctx, config).single().urgencyBucket)
    }

    @Test fun `very overdue is do now`() {
        val ctx = Fixtures.ctx(Fixtures.hive(lastMiteCheckAt = Fixtures.ago(50)))
        assertEquals(UrgencyBucket.DO_NOW, MiteCheckOverdueRule.evaluate(ctx, config).single().urgencyBucket)
    }

    @Test fun `prior high reading escalates a mild overdue to do now`() {
        val ctx = Fixtures.ctx(
            Fixtures.hive(lastMiteCheckAt = Fixtures.ago(25)),
            miteCheck = Fixtures.miteCheck(daysAgo = 25, result = MiteResult.HIGH),
        )
        val rec = MiteCheckOverdueRule.evaluate(ctx, config).single()
        assertEquals(UrgencyBucket.DO_NOW, rec.urgencyBucket)
        assertTrue(rec.longExplanation.contains("high"))
    }

    @Test fun `off-season suppresses routine mite monitoring`() {
        val ctx = Fixtures.ctx(Fixtures.hive(lastMiteCheckAt = Fixtures.ago(50)), month = 1)
        assertTrue(MiteCheckOverdueRule.evaluate(ctx, config).isEmpty())
    }

    // --- Rule 6: post-treatment check ---
    @Test fun `post-treatment check shows due soon before the date`() {
        val hive = Fixtures.hive(
            treatmentState = TreatmentState.FOLLOW_UP_DUE,
            postTreatmentCheckDueAt = Fixtures.ahead(2),
            lastTreatmentEndedAt = Fixtures.ago(5),
        )
        val rec = PostTreatmentCheckRule.evaluate(Fixtures.ctx(hive), config).single()
        assertEquals(UrgencyBucket.DUE_SOON, rec.urgencyBucket)
        assertTrue(rec.longExplanation.contains("confirm the last treatment worked"))
    }

    @Test fun `post-treatment check is do now once due`() {
        val hive = Fixtures.hive(
            treatmentState = TreatmentState.FOLLOW_UP_DUE,
            postTreatmentCheckDueAt = Fixtures.ago(1),
        )
        assertEquals(UrgencyBucket.DO_NOW, PostTreatmentCheckRule.evaluate(Fixtures.ctx(hive), config).single().urgencyBucket)
    }

    @Test fun `no follow-up state produces nothing`() {
        val hive = Fixtures.hive(treatmentState = TreatmentState.NONE, postTreatmentCheckDueAt = Fixtures.ahead(2))
        assertTrue(PostTreatmentCheckRule.evaluate(Fixtures.ctx(hive), config).isEmpty())
    }

    @Test fun `far-future follow-up is not surfaced yet`() {
        val hive = Fixtures.hive(
            treatmentState = TreatmentState.FOLLOW_UP_DUE,
            postTreatmentCheckDueAt = Fixtures.ahead(10),
        )
        assertTrue(PostTreatmentCheckRule.evaluate(Fixtures.ctx(hive), config).isEmpty())
    }
}
