package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.rules.ApiaryContext
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.BestandsbuchRule
import io.github.max_schall.appiary.domain.season.SeasonModel
import io.github.max_schall.appiary.domain.season.SeasonPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BestandsbuchRuleTest {
    private val config = RuleConfig.DEFAULT

    private fun ctx(country: String?, missing: Int, apiaryId: String = "a1") = ApiaryContext(
        apiaryId = apiaryId,
        season = SeasonModel(currentMonth = 7, phase = SeasonPhase.SUMMER_HARVEST, hemisphere = Hemisphere.NORTHERN, climate = null),
        now = Fixtures.NOW,
        countryCode = country,
        treatmentsMissingReceipt = missing,
    )

    @Test fun `flags treatments without a receipt for a German apiary`() {
        val rec = BestandsbuchRule.evaluate(ctx("DE", missing = 2), config).single()
        assertEquals(RecommendationCategory.COMPLIANCE, rec.category)
        assertEquals("a1", rec.apiaryId)
        assertNull(rec.hiveId)
        assertEquals("bestandsbuch:a1", rec.ruleKey)
        assertTrue(rec.title.isNotBlank() && rec.longExplanation.isNotBlank())
    }

    @Test fun `silent when every treatment already has a receipt`() {
        assertTrue(BestandsbuchRule.evaluate(ctx("DE", missing = 0), config).isEmpty())
    }

    @Test fun `does not apply outside Germany`() {
        assertTrue(BestandsbuchRule.evaluate(ctx("AT", missing = 3), config).isEmpty())
        assertTrue(BestandsbuchRule.evaluate(ctx(null, missing = 3), config).isEmpty())
    }
}
