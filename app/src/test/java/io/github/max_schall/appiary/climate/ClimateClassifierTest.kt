package io.github.max_schall.appiary.climate

import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.season.ClimateClassifier
import io.github.max_schall.appiary.domain.season.KoppenGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClimateClassifierTest {

    private fun classify(t: List<Double>, p: List<Double>, h: Hemisphere = Hemisphere.NORTHERN) =
        ClimateClassifier.classify(t, p, h)

    @Test fun `London is temperate oceanic Cfb`() {
        val t = listOf(5.0, 5.0, 7.0, 9.0, 13.0, 16.0, 18.0, 18.0, 15.0, 11.0, 7.0, 5.0)
        val p = listOf(55.0, 40.0, 42.0, 44.0, 49.0, 45.0, 45.0, 50.0, 49.0, 69.0, 59.0, 55.0)
        val (code, group) = classify(t, p)
        assertEquals(KoppenGroup.TEMPERATE, group)
        assertEquals("Cfb", code)
    }

    @Test fun `Singapore is tropical rainforest Af`() {
        val t = List(12) { 27.0 }
        val p = List(12) { 170.0 }
        val (code, group) = classify(t, p)
        assertEquals(KoppenGroup.TROPICAL, group)
        assertEquals("Af", code)
    }

    @Test fun `Cairo is hot desert BWh`() {
        val t = listOf(14.0, 15.0, 18.0, 22.0, 26.0, 28.0, 29.0, 29.0, 27.0, 24.0, 19.0, 15.0)
        val p = listOf(5.0, 4.0, 4.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 4.0, 6.0)
        val (code, group) = classify(t, p)
        assertEquals(KoppenGroup.ARID, group)
        assertEquals("BWh", code)
    }

    @Test fun `Moscow is humid continental Dfb`() {
        val t = listOf(-9.0, -7.0, -1.0, 6.0, 13.0, 17.0, 19.0, 17.0, 11.0, 5.0, -1.0, -6.0)
        val p = listOf(40.0, 35.0, 35.0, 40.0, 50.0, 75.0, 85.0, 75.0, 60.0, 60.0, 50.0, 45.0)
        val (code, group) = classify(t, p)
        assertEquals(KoppenGroup.CONTINENTAL, group)
        assertTrue(code.startsWith("Df"))
    }

    @Test fun `Rome has a dry-summer Mediterranean s code`() {
        val t = listOf(8.0, 9.0, 11.0, 14.0, 18.0, 22.0, 25.0, 25.0, 21.0, 17.0, 12.0, 9.0)
        val p = listOf(80.0, 75.0, 65.0, 70.0, 50.0, 30.0, 18.0, 30.0, 70.0, 110.0, 110.0, 95.0)
        val (code, group) = classify(t, p)
        assertEquals(KoppenGroup.TEMPERATE, group)
        assertTrue("expected Cs*, was $code", code.startsWith("Cs"))
    }

    @Test fun `hardiness zone maps extreme minima to USDA zones`() {
        assertEquals(7, ClimateClassifier.hardinessZone(-15.0)) // ~5 °F
        assertEquals(6, ClimateClassifier.hardinessZone(-20.0)) // ~-4 °F
        assertTrue(ClimateClassifier.hardinessZone(-50.0) <= 3)
        assertTrue(ClimateClassifier.hardinessZone(5.0) >= 9)
    }
}
