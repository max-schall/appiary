package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.rules.ApiaryContext
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.rules.ColdSnapRule
import io.github.max_schall.appiary.domain.rules.rules.InspectionWeatherRule
import io.github.max_schall.appiary.domain.rules.rules.TreatmentWeatherRule
import io.github.max_schall.appiary.domain.season.FlowStatus
import io.github.max_schall.appiary.domain.season.SeasonModel
import io.github.max_schall.appiary.domain.season.SeasonPhase
import io.github.max_schall.appiary.domain.weather.WeatherDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRulesTest {
    private val config = RuleConfig.DEFAULT

    private fun ctx(phase: SeasonPhase, forecast: List<WeatherDay>) = ApiaryContext(
        apiaryId = "a1",
        season = SeasonModel(currentMonth = 5, phase = phase, hemisphere = Hemisphere.NORTHERN, climate = null, flow = FlowStatus.NONE),
        now = Fixtures.NOW,
        forecast = forecast,
    )

    private fun day(off: Int, max: Double, min: Double, precip: Double = 0.0, wind: Double = 5.0) =
        WeatherDay(off, max, min, precip, wind)

    // --- Inspection weather ---
    @Test fun `good window tomorrow during build-up is surfaced`() {
        val recs = InspectionWeatherRule.evaluate(ctx(SeasonPhase.SPRING_BUILDUP, listOf(day(0, 8.0, 2.0), day(1, 20.0, 10.0))), config)
        assertEquals(RecommendationCategory.WEATHER, recs.single().category)
    }

    @Test fun `no good window means no inspection-weather rec`() {
        val recs = InspectionWeatherRule.evaluate(ctx(SeasonPhase.SPRING_BUILDUP, listOf(day(0, 9.0, 2.0, precip = 5.0), day(1, 10.0, 3.0))), config)
        assertTrue(recs.isEmpty())
    }

    @Test fun `inspection weather is silent in winter`() {
        val recs = InspectionWeatherRule.evaluate(ctx(SeasonPhase.WINTER, listOf(day(0, 20.0, 10.0))), config)
        assertTrue(recs.isEmpty())
    }

    @Test fun `no forecast means nothing (offline-safe)`() {
        val recs = InspectionWeatherRule.evaluate(ctx(SeasonPhase.SWARM_AND_FLOW, emptyList()), config)
        assertTrue(recs.isEmpty())
    }

    // --- Treatment heat ---
    @Test fun `heat spell in the treatment season warns`() {
        val recs = TreatmentWeatherRule.evaluate(ctx(SeasonPhase.SUMMER_HARVEST, listOf(day(0, 28.0, 16.0), day(1, 33.0, 18.0))), config)
        assertEquals(1, recs.size)
        assertTrue(recs.single().longExplanation.contains("33"))
    }

    @Test fun `no heat means no treatment-weather warning`() {
        val recs = TreatmentWeatherRule.evaluate(ctx(SeasonPhase.SUMMER_HARVEST, listOf(day(0, 24.0, 14.0))), config)
        assertTrue(recs.isEmpty())
    }

    // --- Cold snap ---
    @Test fun `multi-day cold snap warns`() {
        val recs = ColdSnapRule.evaluate(ctx(SeasonPhase.AUTUMN_PREP, listOf(day(0, 6.0, -2.0), day(1, 5.0, -3.0), day(2, 8.0, 1.0))), config)
        assertEquals(1, recs.size)
    }

    @Test fun `a single cold night does not warn`() {
        val recs = ColdSnapRule.evaluate(ctx(SeasonPhase.AUTUMN_PREP, listOf(day(0, 6.0, -1.0), day(1, 9.0, 4.0))), config)
        assertTrue(recs.isEmpty())
    }
}
