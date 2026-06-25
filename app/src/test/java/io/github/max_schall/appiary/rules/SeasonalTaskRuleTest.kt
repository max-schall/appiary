package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.ApiaryContext
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.SeasonalTaskRule
import io.github.max_schall.appiary.domain.season.PhenologyEngine
import io.github.max_schall.appiary.domain.season.SeasonModel
import io.github.max_schall.appiary.domain.season.SeasonPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalTaskRuleTest {
    private val config = RuleConfig.DEFAULT

    // --- Phase detection (default temperate profile, profile = null) ---
    @Test fun `phase detection across the year matches the consensus calendar`() {
        fun p(m: Int) = PhenologyEngine.phaseFor(m, null)
        assertEquals(SeasonPhase.WINTER, p(1))
        assertEquals(SeasonPhase.WINTER, p(2))
        assertEquals(SeasonPhase.SPRING_BUILDUP, p(3))
        assertEquals(SeasonPhase.SPRING_BUILDUP, p(4))
        assertEquals(SeasonPhase.SWARM_AND_FLOW, p(5))
        assertEquals(SeasonPhase.SWARM_AND_FLOW, p(6))
        assertEquals(SeasonPhase.SUMMER_HARVEST, p(7))
        assertEquals(SeasonPhase.SUMMER_HARVEST, p(8))
        assertEquals(SeasonPhase.AUTUMN_PREP, p(9))
        assertEquals(SeasonPhase.WINTER, p(11))
    }

    private fun ctx(phase: SeasonPhase, apiaryId: String = "a1") = ApiaryContext(
        apiaryId = apiaryId,
        season = SeasonModel(currentMonth = 7, phase = phase, hemisphere = Hemisphere.NORTHERN, climate = null),
        now = Fixtures.NOW,
    )

    @Test fun `emits one apiary-scoped seasonal recommendation per phase`() {
        val rec = SeasonalTaskRule.evaluate(ctx(SeasonPhase.SPRING_BUILDUP), config).single()
        assertEquals(RecommendationCategory.SEASONAL, rec.category)
        assertEquals("a1", rec.apiaryId)
        assertNull(rec.hiveId)
        assertEquals("seasonal_phase:a1", rec.ruleKey)
        assertTrue(rec.title.isNotBlank() && rec.longExplanation.isNotBlank())
    }

    @Test fun `time-critical phases are due soon, the rest are watchlist`() {
        assertEquals(UrgencyBucket.DUE_SOON, SeasonalTaskRule.evaluate(ctx(SeasonPhase.SUMMER_HARVEST), config).single().urgencyBucket)
        assertEquals(UrgencyBucket.DUE_SOON, SeasonalTaskRule.evaluate(ctx(SeasonPhase.AUTUMN_PREP), config).single().urgencyBucket)
        assertEquals(UrgencyBucket.WATCHLIST, SeasonalTaskRule.evaluate(ctx(SeasonPhase.WINTER), config).single().urgencyBucket)
        assertEquals(UrgencyBucket.WATCHLIST, SeasonalTaskRule.evaluate(ctx(SeasonPhase.SPRING_BUILDUP), config).single().urgencyBucket)
    }
}
