package io.github.max_schall.appiary.domain.climate

import io.github.max_schall.appiary.domain.model.Hemisphere
import kotlin.math.abs

/** A season derived from a location, ready to populate a SeasonalProfile. */
data class SeasonalEstimate(
    val hemisphere: Hemisphere,
    val activeStartMonth: Int,
    val activeEndMonth: Int,
    val harvestStartMonth: Int,
    val harvestEndMonth: Int,
    val winterPrepMonth: Int,
)

/**
 * Derives the beekeeping season from a location — **fully offline**. Given only
 * latitude it uses a climate-band heuristic; given monthly mean temperatures
 * (e.g. fetched from the internet when available) it refines the active window
 * to the months bees actually forage (mean temp ≥ [FORAGE_THRESHOLD_C]).
 *
 * Months are 1–12. Harvest is taken as the tail of the active season and winter
 * prep as the month the season closes.
 */
object ClimateEstimator {
    const val FORAGE_THRESHOLD_C = 10.0

    private fun nextMonth(m: Int) = m % 12 + 1
    private fun prevMonth(m: Int) = (m + 10) % 12 + 1
    private fun minusMonths(m: Int, n: Int) = (m - 1 - n + 1200) % 12 + 1

    private fun build(hemisphere: Hemisphere, activeStart: Int, activeEnd: Int): SeasonalEstimate =
        SeasonalEstimate(
            hemisphere = hemisphere,
            activeStartMonth = activeStart,
            activeEndMonth = activeEnd,
            harvestStartMonth = minusMonths(activeEnd, 2),
            harvestEndMonth = activeEnd,
            winterPrepMonth = activeEnd,
        )

    /** Heuristic from latitude only — works everywhere with no network. */
    fun fromLatitude(latitude: Double): SeasonalEstimate {
        val a = abs(latitude)
        // Northern-hemisphere-relative active window by climate band.
        val (startNh, endNh) = when {
            a < 10.0 -> 1 to 12    // tropical: active year-round
            a < 23.5 -> 2 to 11    // subtropical
            a < 35.0 -> 3 to 10    // warm temperate
            a < 45.0 -> 3 to 9     // temperate
            a < 55.0 -> 4 to 9     // cool temperate
            a < 65.0 -> 5 to 8     // cold
            else -> 6 to 7         // polar: very short
        }
        val hemisphere = if (latitude < 0) Hemisphere.SOUTHERN else Hemisphere.NORTHERN
        // Southern hemisphere seasons are offset by half a year.
        val (start, end) = if (hemisphere == Hemisphere.SOUTHERN && a >= 10.0) {
            minusMonths(startNh, -6) to minusMonths(endNh, -6)
        } else {
            startNh to endNh
        }
        return build(hemisphere, start, end)
    }

    /**
     * Refine from 12 monthly mean temperatures (index 0 = January). Falls back
     * to [fromLatitude] if no month is warm enough or data looks incomplete.
     */
    fun fromMonthlyTemps(latitude: Double, monthlyMeanC: List<Double>): SeasonalEstimate {
        if (monthlyMeanC.size != 12) return fromLatitude(latitude)
        val hemisphere = if (latitude < 0) Hemisphere.SOUTHERN else Hemisphere.NORTHERN
        val active = (1..12).filter { monthlyMeanC[it - 1] >= FORAGE_THRESHOLD_C }
        return when {
            active.isEmpty() -> fromLatitude(latitude)
            active.size == 12 -> build(hemisphere, 1, 12)
            else -> {
                val activeSet = active.toSet()
                // Start = an active month whose predecessor is inactive (handles year wrap).
                val start = active.first { prevMonth(it) !in activeSet }
                val end = active.first { nextMonth(it) !in activeSet }
                build(hemisphere, start, end)
            }
        }
    }
}
