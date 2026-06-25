package io.github.max_schall.appiary.domain.season

import io.github.max_schall.appiary.domain.model.Hemisphere

/**
 * Phases of the beekeeping year (Central-European consensus, generalized). Order
 * follows the colony cycle from winter rest through autumn winterization.
 */
enum class SeasonPhase {
    WINTER,          // rest + winter oxalic when broodless
    SPRING_BUILDUP,  // first inspections, build-up
    SWARM_AND_FLOW,  // main flow + swarm season
    SUMMER_HARVEST,  // harvest, then first varroa treatment + feeding
    AUTUMN_PREP,     // finish feeding, treatment check, winterize
}

/**
 * The seasonal context for one apiary at a point in time. Built by
 * [PhenologyEngine] and consumed by apiary-level rules. Flow/dearth fields are
 * added in the bloom phase; for now it carries the phase and climate descriptor.
 */
data class SeasonModel(
    val currentMonth: Int,
    val phase: SeasonPhase,
    val hemisphere: Hemisphere,
    val climate: ClimateProfile?,
    val flow: FlowStatus = FlowStatus.NONE,
    /** Months until the next major flow begins (1..6), when known. */
    val monthsToNextFlow: Int? = null,
) {
    val winterSeverity: WinterSeverity? get() = climate?.winterSeverity
}
