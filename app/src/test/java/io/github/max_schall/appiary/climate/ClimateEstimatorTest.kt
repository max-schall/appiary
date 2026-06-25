package io.github.max_schall.appiary.climate

import io.github.max_schall.appiary.domain.climate.ClimateEstimator
import io.github.max_schall.appiary.domain.model.Hemisphere
import org.junit.Assert.assertEquals
import org.junit.Test

class ClimateEstimatorTest {

    @Test fun `northern temperate latitude gives a spring-to-autumn season`() {
        val e = ClimateEstimator.fromLatitude(48.0)
        assertEquals(Hemisphere.NORTHERN, e.hemisphere)
        assertEquals(4, e.activeStartMonth)
        assertEquals(9, e.activeEndMonth)
        assertEquals(9, e.winterPrepMonth)
        assertEquals(7, e.harvestStartMonth) // end - 2
    }

    @Test fun `southern hemisphere shifts the season by six months`() {
        val e = ClimateEstimator.fromLatitude(-33.8) // Sydney
        assertEquals(Hemisphere.SOUTHERN, e.hemisphere)
        // Northern 3..10 band shifted +6 months → Sep..Apr (wraps year end).
        assertEquals(9, e.activeStartMonth)
        assertEquals(4, e.activeEndMonth)
    }

    @Test fun `tropical latitude is active year round`() {
        val e = ClimateEstimator.fromLatitude(5.0)
        assertEquals(1, e.activeStartMonth)
        assertEquals(12, e.activeEndMonth)
    }

    @Test fun `monthly temps define the active window (northern)`() {
        // Warm Apr–Sep, cold otherwise.
        val temps = listOf(2.0, 3.0, 6.0, 12.0, 16.0, 20.0, 22.0, 21.0, 15.0, 8.0, 4.0, 1.0)
        val e = ClimateEstimator.fromMonthlyTemps(50.0, temps)
        assertEquals(4, e.activeStartMonth)
        assertEquals(9, e.activeEndMonth)
    }

    @Test fun `monthly temps wrap across the year (southern)`() {
        // Warm Nov–Mar, cold Apr–Oct.
        val temps = listOf(20.0, 19.0, 14.0, 8.0, 4.0, 2.0, 1.0, 3.0, 7.0, 9.0, 13.0, 18.0)
        val e = ClimateEstimator.fromMonthlyTemps(-30.0, temps)
        assertEquals(11, e.activeStartMonth)
        assertEquals(3, e.activeEndMonth)
        assertEquals(Hemisphere.SOUTHERN, e.hemisphere)
    }

    @Test fun `no warm month falls back to the latitude heuristic`() {
        val temps = List(12) { -5.0 }
        val e = ClimateEstimator.fromMonthlyTemps(70.0, temps)
        // Polar band 6..7.
        assertEquals(6, e.activeStartMonth)
        assertEquals(7, e.activeEndMonth)
    }
}
