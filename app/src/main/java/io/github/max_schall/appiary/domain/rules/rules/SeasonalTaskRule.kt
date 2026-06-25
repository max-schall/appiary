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

/**
 * Proactive, apiary-level "what to do this part of the season" guidance, derived
 * from the deterministic phenology calendar (distilled from the literature). One
 * recommendation per apiary, updated as the phase advances. Time-critical phases
 * (harvest/treatment, autumn winterization) surface as Due soon; the rest sit on
 * the Watchlist so they inform without nagging.
 */
object SeasonalTaskRule : ApiaryRule {
    override val key = "seasonal_phase"

    override fun evaluate(ctx: ApiaryContext, config: RuleConfig): List<Recommendation> {
        val s = config.strings
        val phase = ctx.season.phase
        val bucket = when (phase) {
            SeasonPhase.SUMMER_HARVEST, SeasonPhase.AUTUMN_PREP -> UrgencyBucket.DUE_SOON
            else -> UrgencyBucket.WATCHLIST
        }
        return listOf(
            Recommendation(
                hiveId = null,
                apiaryId = ctx.apiaryId,
                category = RecommendationCategory.SEASONAL,
                urgencyBucket = bucket,
                urgencyScore = urgencyScore(bucket, 5),
                title = s.seasonalTitle(phase),
                shortReason = s.seasonalShort(phase),
                longExplanation = s.seasonalExplanation(phase),
                dueAt = null,
                // Stable per apiary so it updates in place as the phase changes.
                ruleKey = "$key:${ctx.apiaryId}",
                actionType = ActionType.REVIEW,
            ),
        )
    }
}
