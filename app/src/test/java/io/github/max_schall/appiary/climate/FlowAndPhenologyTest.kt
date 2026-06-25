package io.github.max_schall.appiary.climate

import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.season.BloomCalendars
import io.github.max_schall.appiary.domain.season.FlowDetector
import io.github.max_schall.appiary.domain.season.FlowStatus
import io.github.max_schall.appiary.domain.season.Gdd
import io.github.max_schall.appiary.domain.season.KoppenGroup
import io.github.max_schall.appiary.domain.season.PhenologyEngine
import io.github.max_schall.appiary.domain.season.RegionResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowAndPhenologyTest {

    private val euFlow = FlowDetector.monthlyFlow(BloomCalendars.EUROPE_TEMPERATE, Hemisphere.NORTHERN)

    @Test fun `europe flow months include spring-summer and autumn, with an august dearth`() {
        assertTrue(euFlow[3])  // April — rape/fruit
        assertTrue(euFlow[6])  // July — lime/clover
        assertFalse(euFlow[7]) // August — the Trachtlücke
        assertTrue(euFlow[8])  // September — goldenrod/balsam
        assertFalse(euFlow[1]) // February
    }

    @Test fun `flow status transitions across the season`() {
        assertEquals(FlowStatus.IMMINENT, FlowDetector.status(3, euFlow, isActiveSeason = true)) // pre-flow March
        assertEquals(FlowStatus.ACTIVE, FlowDetector.status(4, euFlow, isActiveSeason = true))   // April flow on
        assertEquals(FlowStatus.DEARTH, FlowDetector.status(8, euFlow, isActiveSeason = true))    // August gap
        assertEquals(FlowStatus.NONE, FlowDetector.status(1, euFlow, isActiveSeason = false))     // winter
    }

    @Test fun `southern hemisphere shifts flow by six months`() {
        val sh = FlowDetector.monthlyFlow(BloomCalendars.EUROPE_TEMPERATE, Hemisphere.SOUTHERN)
        assertTrue(sh[0])   // January (NH July)
        assertFalse(sh[1])  // February (NH August dearth)
    }

    @Test fun `region resolver picks curated calendars then falls back by climate`() {
        assertEquals(BloomCalendars.EUROPE_TEMPERATE.key, RegionResolver.resolve(47.07, 15.44, KoppenGroup.TEMPERATE).key)
        assertEquals(BloomCalendars.NORTH_AMERICA_TEMPERATE.key, RegionResolver.resolve(40.0, -90.0, KoppenGroup.TEMPERATE).key)
        assertEquals(BloomCalendars.generic(KoppenGroup.TROPICAL).key, RegionResolver.resolve(null, null, KoppenGroup.TROPICAL).key)
    }

    @Test fun `phenology model derives flow from the calendar`() {
        val cal = BloomCalendars.EUROPE_TEMPERATE
        assertEquals(FlowStatus.IMMINENT, PhenologyEngine.model(3, null, null, cal).flow)
        assertEquals(FlowStatus.ACTIVE, PhenologyEngine.model(4, null, null, cal).flow)
        assertEquals(FlowStatus.DEARTH, PhenologyEngine.model(8, null, null, cal).flow)
        assertEquals(FlowStatus.NONE, PhenologyEngine.model(1, null, null, cal).flow) // Jan not active
    }

    @Test fun `gdd accumulates above the base and shifts bloom with warmth`() {
        assertEquals(15.0, Gdd.accumulate(listOf(10.0, 10.0, 10.0)), 0.001)
        assertEquals(0.0, Gdd.accumulate(listOf(2.0, 0.0, -5.0)), 0.001)
        assertTrue(Gdd.shiftDays(accumulated = 800.0, reference = 600.0) < 0) // warmer → earlier
    }
}
