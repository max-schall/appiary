package io.github.max_schall.appiary.domain.season

/**
 * Growing-Degree-Day helpers. GDD accumulation tracks how warm the season has
 * actually been, so bloom timing can shift earlier/later than the calendar
 * average when daily temperatures are available (used with the forecast layer).
 */
object Gdd {
    const val DEFAULT_BASE_C = 5.0

    /** Sum of max(0, meanTemp − base) over the given daily means. */
    fun accumulate(dailyMeanC: List<Double>, baseC: Double = DEFAULT_BASE_C): Double =
        dailyMeanC.sumOf { (it - baseC).coerceAtLeast(0.0) }

    /**
     * A coarse phenology shift in days: warmer-than-reference accumulation pulls
     * bloom earlier (negative), cooler pushes it later. ~100 GDD ≈ one week.
     */
    fun shiftDays(accumulated: Double, reference: Double): Int =
        (-(accumulated - reference) / 100.0 * 7.0).toInt().coerceIn(-28, 28)
}
