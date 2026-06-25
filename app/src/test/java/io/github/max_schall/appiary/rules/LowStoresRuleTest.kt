package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.LowStoresRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LowStoresRuleTest {
    private val config = RuleConfig.DEFAULT

    @Test fun `okay stores produce nothing`() {
        val ctx = Fixtures.ctx(Fixtures.hive(foodStores = FoodStores.OKAY))
        assertTrue(LowStoresRule.evaluate(ctx, config).isEmpty())
    }

    @Test fun `low stores in season is due soon`() {
        val ctx = Fixtures.ctx(Fixtures.hive(foodStores = FoodStores.LOW))
        val rec = LowStoresRule.evaluate(ctx, config).single()
        assertEquals(UrgencyBucket.DUE_SOON, rec.urgencyBucket)
        assertTrue(rec.longExplanation.contains("low"))
    }

    @Test fun `low stores in a weak colony is do now`() {
        val ctx = Fixtures.ctx(
            Fixtures.hive(foodStores = FoodStores.LOW, strength = ColonyStrength.WEAK),
        )
        val rec = LowStoresRule.evaluate(ctx, config).single()
        assertEquals(UrgencyBucket.DO_NOW, rec.urgencyBucket)
        assertTrue(rec.longExplanation.contains("weak"))
    }

    @Test fun `low stores off-season is do now`() {
        val ctx = Fixtures.ctx(Fixtures.hive(foodStores = FoodStores.LOW), month = 1)
        assertEquals(UrgencyBucket.DO_NOW, LowStoresRule.evaluate(ctx, config).single().urgencyBucket)
    }
}
