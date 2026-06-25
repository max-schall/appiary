package io.github.max_schall.appiary.domain.season

import io.github.max_schall.appiary.data.entity.SeasonalProfileEntity
import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.rules.Season

/**
 * Derives the season phase (and assembles the [SeasonModel]) from the calendar
 * month and a [SeasonalProfileEntity]. Pure and testable. Phase boundaries are
 * anchored to the profile's active-season start and harvest start, so they scale
 * to any location's profile (default = Central-European temperate).
 */
object PhenologyEngine {

    private fun addMonths(month: Int, n: Int): Int = ((month - 1 + n) % 12 + 12) % 12 + 1

    fun phaseFor(month: Int, profile: SeasonalProfileEntity?): SeasonPhase {
        if (!Season.isActiveSeason(month, profile)) return SeasonPhase.WINTER
        val activeStart = profile?.activeSeasonStartMonth ?: 3
        val harvestStart = profile?.harvestStartMonth ?: 7
        return when {
            Season.monthInRange(month, activeStart, addMonths(activeStart, 1)) -> SeasonPhase.SPRING_BUILDUP
            Season.monthInRange(month, addMonths(activeStart, 2), addMonths(harvestStart, -1)) -> SeasonPhase.SWARM_AND_FLOW
            Season.monthInRange(month, harvestStart, addMonths(harvestStart, 1)) -> SeasonPhase.SUMMER_HARVEST
            else -> SeasonPhase.AUTUMN_PREP
        }
    }

    fun model(
        month: Int,
        profile: SeasonalProfileEntity?,
        climate: ClimateProfile?,
        calendar: BloomCalendar? = null,
    ): SeasonModel {
        val hemisphere = climate?.hemisphere
            ?: profile?.hemisphere
            ?: Hemisphere.NORTHERN
        val active = Season.isActiveSeason(month, profile)
        val (flow, nextFlow) = if (calendar != null) {
            val monthly = FlowDetector.monthlyFlow(calendar, hemisphere)
            FlowDetector.status(month, monthly, active) to FlowDetector.nextFlowInMonths(month, monthly)
        } else {
            FlowStatus.NONE to null
        }
        return SeasonModel(
            currentMonth = month,
            phase = phaseFor(month, profile),
            hemisphere = hemisphere,
            climate = climate,
            flow = flow,
            monthsToNextFlow = nextFlow,
        )
    }
}
