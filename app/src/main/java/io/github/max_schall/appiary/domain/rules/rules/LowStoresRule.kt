package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.HiveContext
import io.github.max_schall.appiary.domain.rules.HiveRule
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.Season
import io.github.max_schall.appiary.domain.rules.urgencyScore
import io.github.max_schall.appiary.util.TimeUtil

/**
 * Rule 4 — Low stores / feeding follow-up.
 * Low food stores at the last inspection prompt feeding. Escalates to Do now
 * for a weak colony or outside the active season, when running out is dangerous.
 */
object LowStoresRule : HiveRule {
    override val key = "low_stores"

    override fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation> {
        val hive = ctx.hive
        if (hive.foodStores != FoodStores.LOW) return emptyList()

        val offSeason = !Season.isActiveSeason(ctx.currentMonth, ctx.seasonal)
        val weak = hive.strength == ColonyStrength.WEAK
        val urgent = weak || offSeason
        val bucket = if (urgent) UrgencyBucket.DO_NOW else UrgencyBucket.DUE_SOON
        val severity = if (urgent) 20 else 10
        val daysSince = hive.lastInspectionAt?.let { TimeUtil.daysBetween(it, ctx.now) }
        val s = config.strings

        return listOf(
            Recommendation(
                hiveId = hive.id,
                apiaryId = hive.apiaryId,
                category = RecommendationCategory.FEEDING,
                urgencyBucket = bucket,
                urgencyScore = urgencyScore(bucket, severity),
                title = s.feedTitle(hive.name),
                shortReason = s.feedShort(),
                longExplanation = s.lowStoresExplanation(daysSince, weak, offSeason),
                dueAt = ctx.now,
                ruleKey = key,
                actionType = ActionType.LOG_FEEDING,
            ),
        )
    }
}
