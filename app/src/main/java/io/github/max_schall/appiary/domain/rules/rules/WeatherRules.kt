package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.ApiaryContext
import io.github.max_schall.appiary.domain.rules.ApiaryRule
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.urgencyScore
import io.github.max_schall.appiary.domain.season.SeasonPhase

private fun weatherRec(
    apiaryId: String,
    ruleKey: String,
    title: String,
    short: String,
    explanation: String,
    severity: Int = 8,
) = Recommendation(
    hiveId = null,
    apiaryId = apiaryId,
    category = RecommendationCategory.WEATHER,
    urgencyBucket = UrgencyBucket.WATCHLIST,
    urgencyScore = urgencyScore(UrgencyBucket.WATCHLIST, severity),
    title = title,
    shortReason = short,
    longExplanation = explanation,
    dueAt = null,
    ruleKey = "$ruleKey:$apiaryId",
    actionType = ActionType.REVIEW,
)

/**
 * Flags an imminent good-weather window for inspecting (warm, calm, dry) when
 * the colony would actually be worked. Forecast-driven and online-only — absent
 * a forecast it produces nothing. Limited to today/tomorrow so it stays timely.
 */
object InspectionWeatherRule : ApiaryRule {
    override val key = "weather_inspection"
    private val inspectionPhases = setOf(
        SeasonPhase.SPRING_BUILDUP, SeasonPhase.SWARM_AND_FLOW, SeasonPhase.SUMMER_HARVEST,
    )

    override fun evaluate(ctx: ApiaryContext, config: RuleConfig): List<Recommendation> {
        if (ctx.season.phase !in inspectionPhases || ctx.forecast.isEmpty()) return emptyList()
        val good = ctx.forecast.firstOrNull {
            it.dayOffset <= 1 &&
                it.tempMaxC >= config.inspectionMinTempC &&
                it.windMaxKmh <= config.inspectionMaxWindKmh &&
                it.precipMm <= config.inspectionMaxPrecipMm
        } ?: return emptyList()
        val s = config.strings
        return listOf(
            weatherRec(
                ctx.apiaryId, key,
                s.inspectionWeatherTitle(), s.inspectionWeatherShort(),
                s.inspectionWeatherExplanation(good.dayOffset),
            ),
        )
    }
}

/** Warns of a heat spell during the treatment season (formic acid risk). */
object TreatmentWeatherRule : ApiaryRule {
    override val key = "weather_treatment"
    private val treatmentPhases = setOf(SeasonPhase.SUMMER_HARVEST, SeasonPhase.AUTUMN_PREP)

    override fun evaluate(ctx: ApiaryContext, config: RuleConfig): List<Recommendation> {
        if (ctx.season.phase !in treatmentPhases || ctx.forecast.isEmpty()) return emptyList()
        val hottest = ctx.forecast.maxByOrNull { it.tempMaxC } ?: return emptyList()
        if (hottest.tempMaxC <= config.treatmentHeatMaxTempC) return emptyList()
        val s = config.strings
        return listOf(
            weatherRec(
                ctx.apiaryId, key,
                s.treatmentWeatherTitle(), s.treatmentWeatherShort(),
                s.treatmentWeatherExplanation(hottest.tempMaxC), severity = 12,
            ),
        )
    }
}

/** Warns of a multi-day cold snap (stores + shelter, hold off inspections). */
object ColdSnapRule : ApiaryRule {
    override val key = "weather_cold"

    override fun evaluate(ctx: ApiaryContext, config: RuleConfig): List<Recommendation> {
        if (ctx.forecast.isEmpty()) return emptyList()
        val coldDays = ctx.forecast.filter { it.tempMinC <= config.coldSnapMinTempC }
        if (coldDays.size < 2) return emptyList()
        val s = config.strings
        val coldest = coldDays.minOf { it.tempMinC }
        return listOf(
            weatherRec(
                ctx.apiaryId, key,
                s.coldSnapTitle(), s.coldSnapShort(), s.coldSnapExplanation(coldest), severity = 12,
            ),
        )
    }
}
