package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.SwarmRiskRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwarmRiskRuleTest {
    private val config = RuleConfig.DEFAULT

    @Test fun `no signs produces nothing`() {
        val ctx = Fixtures.ctx(Fixtures.hive(), inspections = listOf(Fixtures.inspection(daysAgo = 2)))
        assertTrue(SwarmRiskRule.evaluate(ctx, config).isEmpty())
    }

    @Test fun `queen cells are always do now with max severity`() {
        val ctx = Fixtures.ctx(
            Fixtures.hive(),
            inspections = listOf(Fixtures.inspection(daysAgo = 1, queenCells = true)),
        )
        val rec = SwarmRiskRule.evaluate(ctx, config).single()
        assertEquals(UrgencyBucket.DO_NOW, rec.urgencyBucket)
        assertTrue(rec.shortReason.contains("Queen cells"))
        assertTrue(rec.longExplanation.contains("can swarm within days"))
    }

    @Test fun `recent swarm signs without cells are due soon`() {
        val ctx = Fixtures.ctx(
            Fixtures.hive(),
            inspections = listOf(Fixtures.inspection(daysAgo = 2, swarmSigns = true)),
        )
        assertEquals(UrgencyBucket.DUE_SOON, SwarmRiskRule.evaluate(ctx, config).single().urgencyBucket)
    }

    @Test fun `stale swarm signs escalate to do now`() {
        val ctx = Fixtures.ctx(
            Fixtures.hive(),
            inspections = listOf(Fixtures.inspection(daysAgo = 7, swarmSigns = true)),
        )
        assertEquals(UrgencyBucket.DO_NOW, SwarmRiskRule.evaluate(ctx, config).single().urgencyBucket)
    }
}
