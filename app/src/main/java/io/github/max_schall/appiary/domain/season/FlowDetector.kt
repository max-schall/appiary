package io.github.max_schall.appiary.domain.season

import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.rules.Season

/** Nectar-flow state at a point in the season. */
enum class FlowStatus {
    NONE,       // off-season / no flow context
    IMMINENT,   // a major flow starts within ~a month (pre-flow: super & swarm-watch)
    ACTIVE,     // a major flow is on now
    DEARTH,     // active season, gap between flows (robbing risk, feed, treatment window)
}

/**
 * Detects flow/dearth from a [BloomCalendar]. Pure. Calendars are stored
 * Northern-hemisphere-relative; for the southern hemisphere the monthly flow
 * pattern is rotated by six months.
 */
object FlowDetector {

    /** Boolean per month (index 0 = Jan) for "a major flow is blooming". */
    fun monthlyFlow(calendar: BloomCalendar, hemisphere: Hemisphere): BooleanArray {
        val nh = BooleanArray(12)
        for (m in 1..12) {
            nh[m - 1] = calendar.sources.any { it.majorFlow && Season.monthInRange(m, it.startMonth, it.endMonth) }
        }
        if (hemisphere == Hemisphere.NORTHERN) return nh
        return BooleanArray(12) { nh[(it + 6) % 12] } // shift +6 months for SH
    }

    private fun monthsToNextFlow(month: Int, flow: BooleanArray): Int? =
        (1..6).firstOrNull { flow[(month - 1 + it) % 12] }

    private fun monthsSinceLastFlow(month: Int, flow: BooleanArray): Int? =
        (1..6).firstOrNull { flow[(month - 1 - it + 12) % 12] }

    fun status(month: Int, flow: BooleanArray, isActiveSeason: Boolean): FlowStatus {
        if (!isActiveSeason) return FlowStatus.NONE
        if (flow[month - 1]) return FlowStatus.ACTIVE
        val next = monthsToNextFlow(month, flow)
        val since = monthsSinceLastFlow(month, flow)
        return when {
            // A flow that just ended is a dearth (robbing risk + treatment window),
            // even if a minor flow is approaching.
            since != null && since <= 1 -> FlowStatus.DEARTH
            next == 1 -> FlowStatus.IMMINENT
            since != null && since <= 2 -> FlowStatus.DEARTH
            next != null -> FlowStatus.IMMINENT // pre-flow build-up
            else -> FlowStatus.NONE
        }
    }

    /** Months until the next major flow begins (1..6), or null. */
    fun nextFlowInMonths(month: Int, flow: BooleanArray): Int? = monthsToNextFlow(month, flow)
}
