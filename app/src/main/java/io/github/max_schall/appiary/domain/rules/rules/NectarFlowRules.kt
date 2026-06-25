package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.ApiaryContext
import io.github.max_schall.appiary.domain.rules.ApiaryRule
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.urgencyScore
import io.github.max_schall.appiary.domain.season.FlowStatus

/**
 * Pre-flow heads-up: when a major nectar flow is approaching, prompt supering and
 * stepped-up swarm control so the colony captures the flow without swarming.
 */
object NectarFlowRule : ApiaryRule {
    override val key = "nectar_flow"

    override fun evaluate(ctx: ApiaryContext, config: RuleConfig): List<Recommendation> {
        if (ctx.season.flow != FlowStatus.IMMINENT) return emptyList()
        val s = config.strings
        return listOf(
            Recommendation(
                hiveId = null,
                apiaryId = ctx.apiaryId,
                category = RecommendationCategory.NECTAR_FLOW,
                urgencyBucket = UrgencyBucket.DUE_SOON,
                urgencyScore = urgencyScore(UrgencyBucket.DUE_SOON, 12),
                title = s.flowImminentTitle(),
                shortReason = s.flowImminentShort(),
                longExplanation = s.flowImminentExplanation(ctx.season.monthsToNextFlow),
                dueAt = null,
                ruleKey = "$key:${ctx.apiaryId}",
                actionType = ActionType.REVIEW,
            ),
        )
    }
}

/**
 * Forage dearth: a gap between flows raises robbing risk and may need feeding;
 * with supers off it's also a good varroa-treatment window.
 */
object DearthRule : ApiaryRule {
    override val key = "dearth"

    override fun evaluate(ctx: ApiaryContext, config: RuleConfig): List<Recommendation> {
        if (ctx.season.flow != FlowStatus.DEARTH) return emptyList()
        val s = config.strings
        return listOf(
            Recommendation(
                hiveId = null,
                apiaryId = ctx.apiaryId,
                category = RecommendationCategory.NECTAR_FLOW,
                urgencyBucket = UrgencyBucket.WATCHLIST,
                urgencyScore = urgencyScore(UrgencyBucket.WATCHLIST, 12),
                title = s.dearthTitle(),
                shortReason = s.dearthShort(),
                longExplanation = s.dearthExplanation(),
                dueAt = null,
                ruleKey = "$key:${ctx.apiaryId}",
                actionType = ActionType.REVIEW,
            ),
        )
    }
}
