package io.github.max_schall.appiary.domain.rules

import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.data.entity.SeasonalProfileEntity
import io.github.max_schall.appiary.data.entity.TreatmentEventEntity

/**
 * Everything the rules need to evaluate a single hive. Built once per engine
 * run and passed (read-only) to every [HiveRule], so rules never touch the
 * database and stay pure/testable.
 */
data class HiveContext(
    val hive: HiveEntity,
    /** Newest-first; index 0 is the most recent inspection. */
    val recentInspections: List<InspectionEntity>,
    val latestMiteCheck: MiteCheckEntity?,
    val latestTreatment: TreatmentEventEntity?,
    val now: Long,
    /** Calendar month 1-12, supplied by the caller (keeps rules timezone-pure). */
    val currentMonth: Int,
    val seasonal: SeasonalProfileEntity?,
) {
    val latestInspection: InspectionEntity? get() = recentInspections.firstOrNull()
}

/** Location-level context for apiary-scoped (seasonal/weather) rules. */
data class ApiaryContext(
    val apiaryId: String,
    val season: io.github.max_schall.appiary.domain.season.SeasonModel,
    val now: Long,
    /** Short-range forecast (empty when offline / no coordinates). */
    val forecast: List<io.github.max_schall.appiary.domain.weather.WeatherDay> = emptyList(),
    /** ISO country of the apiary's site (drives jurisdiction-specific rules). */
    val countryCode: String? = null,
    /** Treatments at this apiary still lacking a proof-of-purchase receipt. */
    val treatmentsMissingReceipt: Int = 0,
)

/** Full input to a single engine run. */
data class EvaluationInput(
    val hiveContexts: List<HiveContext>,
    val apiaryContexts: List<ApiaryContext> = emptyList(),
    val openTasks: List<ManualTaskEntity>,
    val now: Long,
)
