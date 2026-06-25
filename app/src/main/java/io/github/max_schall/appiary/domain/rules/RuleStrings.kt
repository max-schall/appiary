package io.github.max_schall.appiary.domain.rules

import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.season.SeasonPhase

/**
 * All user-facing text the rules engine produces, behind an interface so it can
 * be localized without coupling the (pure, testable) engine to Android. There
 * is one implementation per supported language ([DefaultRuleStrings] = English,
 * [GermanRuleStrings] = German); the app selects one by the active locale and
 * recommendations are regenerated when the language changes.
 */
interface RuleStrings {
    // Relative-time phrasing
    fun daysAgo(days: Long): String
    fun inDays(days: Long): String
    fun dayCount(days: Long): String

    // Rule 1 — inspection overdue
    fun inspectTitle(hiveName: String): String
    fun inspectShort(overdue: Boolean, days: Long): String
    fun inspectExplanation(neverInspected: Boolean, daysSince: Long, intervalDays: Int, queenUnsure: Boolean): String

    // Rule 2 — queen uncertainty
    fun queenTitle(hiveName: String, queenless: Boolean): String
    fun queenShort(queenless: Boolean): String
    fun queenExplanation(queenless: Boolean, daysSince: Long, followUpDays: Int): String

    // Rule 9 — repeated queen uncertainty
    fun repeatedQueenTitle(hiveName: String): String
    fun repeatedQueenShort(streak: Int): String
    fun repeatedQueenExplanation(streak: Int, spanDays: Long): String

    // Rule 3 — swarm risk
    fun swarmTitle(hiveName: String): String
    fun swarmShort(cells: Boolean): String
    fun swarmExplanation(cells: Boolean, bothCellsAndSigns: Boolean, daysSince: Long, followUpDays: Int): String

    // Rule 4 — low stores
    fun feedTitle(hiveName: String): String
    fun feedShort(): String
    fun lowStoresExplanation(daysSince: Long?, weak: Boolean, offSeason: Boolean): String

    // Rule 5 — mite check overdue
    fun miteCheckTitle(hiveName: String): String
    fun miteOverdueShort(never: Boolean, overdueDays: Long): String
    fun miteOverdueExplanation(never: Boolean, daysSince: Long, intervalDays: Int, priorHighResult: MiteResult?): String

    // Rule 6 — post-treatment check
    fun postTreatmentTitle(hiveName: String): String
    fun postTreatmentShort(daysUntil: Long): String
    fun postTreatmentExplanation(daysUntil: Long, endedDays: Long?): String

    // Rule 7 — weak colony
    fun weakTitle(hiveName: String): String
    fun weakShort(): String
    fun weakExplanation(daysSince: Long?): String

    // Rule 8 — harvest prep
    fun harvestTitle(hiveName: String): String
    fun harvestShort(inWindow: Boolean): String
    fun harvestExplanation(inWindow: Boolean): String

    // Rule 10 — manual follow-up
    fun manualShort(overdue: Boolean, daysUntil: Long): String
    fun manualExplanation(overdue: Boolean, daysUntil: Long, details: String?): String

    /** Localized lowercase name of a mite result (for inline use in sentences). */
    fun miteResultLower(result: MiteResult): String

    // Seasonal task calendar (apiary-level)
    fun seasonalTitle(phase: SeasonPhase): String
    fun seasonalShort(phase: SeasonPhase): String
    fun seasonalExplanation(phase: SeasonPhase): String

    // Nectar flow / dearth (apiary-level)
    fun flowImminentTitle(): String
    fun flowImminentShort(): String
    fun flowImminentExplanation(monthsToNext: Int?): String
    fun dearthTitle(): String
    fun dearthShort(): String
    fun dearthExplanation(): String

    // Weather-timed (apiary-level, forecast-driven)
    fun inspectionWeatherTitle(): String
    fun inspectionWeatherShort(): String
    fun inspectionWeatherExplanation(dayOffset: Int): String
    fun treatmentWeatherTitle(): String
    fun treatmentWeatherShort(): String
    fun treatmentWeatherExplanation(maxTempC: Double): String
    fun coldSnapTitle(): String
    fun coldSnapShort(): String
    fun coldSnapExplanation(minTempC: Double): String

    // German Bestandsbuch (treatment record book)
    fun bestandsbuchTitle(): String
    fun bestandsbuchShort(missingCount: Int): String
    fun bestandsbuchExplanation(missingCount: Int): String
}
