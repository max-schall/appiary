package io.github.max_schall.appiary.domain.rules

/**
 * Tunable thresholds for the rules engine. Every number a rule compares against
 * lives here so behavior is configurable (Settings screen, Phase 6) and tests
 * can pin exact values. Defaults reflect temperate-climate practice.
 */
data class RuleConfig(
    /** Inspection cadence during the active season vs. off-season (days). */
    val activeSeasonInspectionIntervalDays: Int = 14,
    val offSeasonInspectionIntervalDays: Int = 30,
    /** How close to "due" something must be before it shows as Due soon. */
    val dueSoonWindowDays: Int = 3,
    /** Days overdue past the interval before an inspection escalates to Do now. */
    val inspectionDoNowOverdueDays: Int = 7,

    /** Re-confirm a queen within this many days of an uncertain sighting. */
    val queenFollowUpDays: Int = 7,
    /** Consecutive uncertain inspections that escalate to a queen-failure action. */
    val repeatedQueenUncertaintyCount: Int = 2,

    /** Re-check a swarm-signs colony within this many days. */
    val swarmFollowUpDays: Int = 5,

    /** Varroa monitoring cadence during the active season (days). */
    val miteCheckIntervalDays: Int = 21,

    /** Whether weak colonies are surfaced on the watchlist. */
    val watchWeakColonies: Boolean = true,

    // --- Weather-timed thresholds (forecast layer) ---
    /** A day is "good for inspecting" at/above this high, calm and dry. */
    val inspectionMinTempC: Double = 15.0,
    val inspectionMaxWindKmh: Double = 25.0,
    val inspectionMaxPrecipMm: Double = 1.0,
    /** Highs above this make formic-acid treatment risky. */
    val treatmentHeatMaxTempC: Double = 30.0,
    /** Lows at/below this (for ≥2 forecast days) count as a cold snap. */
    val coldSnapMinTempC: Double = 0.0,

    /** Localized text producer for recommendations (defaults to English). Not
     *  persisted — the app sets this per active locale at engine-run time. */
    val strings: RuleStrings = DefaultRuleStrings,
) {
    companion object {
        val DEFAULT = RuleConfig()
    }
}
