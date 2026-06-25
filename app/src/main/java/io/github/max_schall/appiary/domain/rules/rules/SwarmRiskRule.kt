package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.HiveContext
import io.github.max_schall.appiary.domain.rules.HiveRule
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.urgencyScore
import io.github.max_schall.appiary.util.TimeUtil

/**
 * Rule 3 — Swarm-risk follow-up.
 * Queen cells seen on the last visit mean a swarm could issue within days →
 * Do now. Swarm signs without cells warrant a prompt re-check within the
 * follow-up window.
 */
object SwarmRiskRule : HiveRule {
    override val key = "swarm_risk"

    override fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation> {
        val last = ctx.latestInspection ?: return emptyList()
        if (!last.swarmSigns && !last.queenCells) return emptyList()

        val daysSince = TimeUtil.daysBetween(last.performedAt, ctx.now)
        val cells = last.queenCells

        val bucket = when {
            cells -> UrgencyBucket.DO_NOW
            daysSince >= config.swarmFollowUpDays -> UrgencyBucket.DO_NOW
            else -> UrgencyBucket.DUE_SOON
        }
        val severity = if (cells) 30 else (15 + daysSince).coerceIn(0, 30).toInt()
        val s = config.strings

        return listOf(
            Recommendation(
                hiveId = ctx.hive.id,
                apiaryId = ctx.hive.apiaryId,
                category = RecommendationCategory.SWARM,
                urgencyBucket = bucket,
                urgencyScore = urgencyScore(bucket, severity),
                title = s.swarmTitle(ctx.hive.name),
                shortReason = s.swarmShort(cells),
                longExplanation = s.swarmExplanation(
                    cells = cells,
                    bothCellsAndSigns = cells && last.swarmSigns,
                    daysSince = daysSince,
                    followUpDays = config.swarmFollowUpDays,
                ),
                dueAt = last.performedAt + TimeUtil.days(config.swarmFollowUpDays.toLong()),
                ruleKey = key,
                actionType = ActionType.LOG_INSPECTION,
            ),
        )
    }
}
