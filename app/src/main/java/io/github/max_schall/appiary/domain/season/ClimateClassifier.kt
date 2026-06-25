package io.github.max_schall.appiary.domain.season

import io.github.max_schall.appiary.domain.model.Hemisphere

/**
 * Pure, offline Köppen–Geiger climate classification + USDA-style hardiness zone,
 * computed from 12 monthly mean temperatures (°C) and 12 monthly precipitation
 * totals (mm). Follows the standard Peel et al. (2007) thresholds. Globally
 * applicable; no network, no Android dependencies (so it's unit-tested directly).
 */
object ClimateClassifier {

    /** Months (1-12) of the high-sun (warm) half-year for the hemisphere. */
    private fun summerMonths(h: Hemisphere): List<Int> =
        if (h == Hemisphere.NORTHERN) listOf(4, 5, 6, 7, 8, 9) else listOf(10, 11, 12, 1, 2, 3)

    fun classify(monthlyTempC: List<Double>, monthlyPrecipMm: List<Double>, hemisphere: Hemisphere): Pair<String, KoppenGroup> {
        require(monthlyTempC.size == 12 && monthlyPrecipMm.size == 12)
        val mat = monthlyTempC.average()
        val map = monthlyPrecipMm.sum()
        val tCold = monthlyTempC.min()
        val tHot = monthlyTempC.max()
        val monthsAbove10 = monthlyTempC.count { it >= 10.0 }

        val summer = summerMonths(hemisphere).map { it - 1 }.toSet()
        val pSummer = monthlyPrecipMm.filterIndexed { i, _ -> i in summer }.sum()
        val pWinter = map - pSummer

        // Aridity threshold (mm).
        val adj = when {
            pSummer >= 0.70 * map -> 280.0
            pWinter >= 0.70 * map -> 0.0
            else -> 140.0
        }
        val threshold = 20.0 * mat + adj

        // --- B: arid ---
        if (map < threshold) {
            val first = if (map < threshold / 2.0) "BW" else "BS"
            val second = if (mat >= 18.0) "h" else "k"
            return (first + second) to KoppenGroup.ARID
        }

        // --- A: tropical ---
        if (tCold >= 18.0) {
            val pDriest = monthlyPrecipMm.min()
            val code = when {
                pDriest >= 60.0 -> "Af"
                pDriest >= 100.0 - map / 25.0 -> "Am"
                else -> "Aw"
            }
            return code to KoppenGroup.TROPICAL
        }

        // --- E: polar ---
        if (tHot < 10.0) {
            return (if (tHot >= 0.0) "ET" else "EF") to KoppenGroup.POLAR
        }

        // --- C / D ---
        val summerP = monthlyPrecipMm.filterIndexed { i, _ -> i in summer }
        val winterP = monthlyPrecipMm.filterIndexed { i, _ -> i !in summer }
        val pSdry = summerP.min(); val pSwet = summerP.max()
        val pWdry = winterP.min(); val pWwet = winterP.max()
        val precipLetter = when {
            pSdry < 40.0 && pSdry < pWwet / 3.0 -> "s"
            pWdry < pSwet / 10.0 -> "w"
            else -> "f"
        }
        val heatLetter = when {
            tHot >= 22.0 -> "a"
            monthsAbove10 >= 4 -> "b"
            else -> "c"
        }
        return if (tCold > 0.0) {
            ("C$precipLetter$heatLetter") to KoppenGroup.TEMPERATE
        } else {
            val d = if (tCold < -38.0) "d" else heatLetter
            ("D$precipLetter$d") to KoppenGroup.CONTINENTAL
        }
    }

    /** USDA hardiness zone 1–13 from the annual extreme-minimum temperature (°C).
     *  Zone 1 = −60..−50 °F, zone 13 = 60..70 °F. */
    fun hardinessZone(annualMinTempC: Double): Int {
        val f = annualMinTempC * 9.0 / 5.0 + 32.0
        return (Math.floor((f + 60.0) / 10.0).toInt() + 1).coerceIn(1, 13)
    }

    fun winterSeverity(zone: Int): WinterSeverity = when {
        zone <= 4 -> WinterSeverity.HARSH
        zone <= 7 -> WinterSeverity.MODERATE
        else -> WinterSeverity.MILD
    }

    fun profile(
        monthlyTempC: List<Double>,
        monthlyPrecipMm: List<Double>,
        annualMinTempC: Double,
        hemisphere: Hemisphere,
    ): ClimateProfile {
        val (code, group) = classify(monthlyTempC, monthlyPrecipMm, hemisphere)
        val zone = hardinessZone(annualMinTempC)
        return ClimateProfile(code, group, zone, winterSeverity(zone), hemisphere)
    }
}
