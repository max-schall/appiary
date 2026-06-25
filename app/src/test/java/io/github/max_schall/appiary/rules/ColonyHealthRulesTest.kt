package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.HiveStatus
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.HarvestPrepRule
import io.github.max_schall.appiary.domain.rules.rules.WeakColonyRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColonyHealthRulesTest {
    private val config = RuleConfig.DEFAULT

    // --- Rule 7: weak colony ---
    @Test fun `weak strength goes on the watchlist`() {
        val ctx = Fixtures.ctx(Fixtures.hive(strength = ColonyStrength.WEAK))
        assertEquals(UrgencyBucket.WATCHLIST, WeakColonyRule.evaluate(ctx, config).single().urgencyBucket)
    }

    @Test fun `weak status also triggers`() {
        val ctx = Fixtures.ctx(Fixtures.hive(status = HiveStatus.WEAK, strength = ColonyStrength.MODERATE))
        assertEquals(1, WeakColonyRule.evaluate(ctx, config).size)
    }

    @Test fun `strong colony is fine`() {
        assertTrue(WeakColonyRule.evaluate(Fixtures.ctx(Fixtures.hive()), config).isEmpty())
    }

    @Test fun `disabling the watch suppresses it`() {
        val ctx = Fixtures.ctx(Fixtures.hive(strength = ColonyStrength.WEAK))
        assertTrue(WeakColonyRule.evaluate(ctx, config.copy(watchWeakColonies = false)).isEmpty())
    }

    // --- Rule 8: harvest prep ---
    private fun productive() = Fixtures.hive(strength = ColonyStrength.STRONG, foodStores = FoodStores.STRONG)

    @Test fun `prep month watchlists productive colonies`() {
        val ctx = Fixtures.ctx(productive(), month = 6, profile = Fixtures.profile()) // harvest starts month 7
        val rec = HarvestPrepRule.evaluate(ctx, config).single()
        assertEquals(UrgencyBucket.WATCHLIST, rec.urgencyBucket)
    }

    @Test fun `harvest window is due soon`() {
        val ctx = Fixtures.ctx(productive(), month = 8, profile = Fixtures.profile())
        assertEquals(UrgencyBucket.DUE_SOON, HarvestPrepRule.evaluate(ctx, config).single().urgencyBucket)
    }

    @Test fun `non-productive colony is ignored even in window`() {
        val ctx = Fixtures.ctx(Fixtures.hive(foodStores = FoodStores.OKAY), month = 8, profile = Fixtures.profile())
        assertTrue(HarvestPrepRule.evaluate(ctx, config).isEmpty())
    }

    @Test fun `off-harvest months produce nothing`() {
        val ctx = Fixtures.ctx(productive(), month = 3, profile = Fixtures.profile())
        assertTrue(HarvestPrepRule.evaluate(ctx, config).isEmpty())
    }
}
