package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.TaskStatus
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.ManualFollowUpRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualFollowUpRuleTest {
    private val config = RuleConfig.DEFAULT

    @Test fun `overdue open task is do now`() {
        val recs = ManualFollowUpRule.evaluate(listOf(Fixtures.task(dueAt = Fixtures.ago(2))), config, Fixtures.NOW)
        assertEquals(UrgencyBucket.DO_NOW, recs.single().urgencyBucket)
        assertTrue(recs.single().shortReason.contains("Overdue"))
    }

    @Test fun `soon-due task is due soon`() {
        val recs = ManualFollowUpRule.evaluate(listOf(Fixtures.task(dueAt = Fixtures.ahead(2))), config, Fixtures.NOW)
        assertEquals(UrgencyBucket.DUE_SOON, recs.single().urgencyBucket)
    }

    @Test fun `far-future task is not surfaced`() {
        val recs = ManualFollowUpRule.evaluate(listOf(Fixtures.task(dueAt = Fixtures.ahead(10))), config, Fixtures.NOW)
        assertTrue(recs.isEmpty())
    }

    @Test fun `task without a due date is ignored`() {
        val recs = ManualFollowUpRule.evaluate(listOf(Fixtures.task(dueAt = null)), config, Fixtures.NOW)
        assertTrue(recs.isEmpty())
    }

    @Test fun `non-open tasks are ignored`() {
        val recs = ManualFollowUpRule.evaluate(
            listOf(Fixtures.task(dueAt = Fixtures.ago(2), status = TaskStatus.DONE)),
            config, Fixtures.NOW,
        )
        assertTrue(recs.isEmpty())
    }

    @Test fun `rule key is unique per task for reconciliation`() {
        val recs = ManualFollowUpRule.evaluate(
            listOf(
                Fixtures.task(id = "t1", dueAt = Fixtures.ago(1)),
                Fixtures.task(id = "t2", dueAt = Fixtures.ago(1)),
            ),
            config, Fixtures.NOW,
        )
        assertEquals(2, recs.size)
        assertEquals(setOf("manual_overdue:t1", "manual_overdue:t2"), recs.map { it.ruleKey }.toSet())
    }
}
