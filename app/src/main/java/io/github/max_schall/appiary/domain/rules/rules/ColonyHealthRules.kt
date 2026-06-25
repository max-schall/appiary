package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.HiveStatus
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
 * Rule 7 — Weak colony watch.
 * A weak colony isn't an emergency by itself, but it should stay on the radar.
 * Surfaces on the Watchlist (unless config disables it).
 */
object WeakColonyRule : HiveRule {
    override val key = "weak_colony"

    override fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation> {
        if (!config.watchWeakColonies) return emptyList()
        val hive = ctx.hive
        val weak = hive.strength == ColonyStrength.WEAK || hive.status == HiveStatus.WEAK
        if (!weak) return emptyList()

        val daysSince = hive.lastInspectionAt?.let { TimeUtil.daysBetween(it, ctx.now) }
        val s = config.strings

        return listOf(
            Recommendation(
                hiveId = hive.id,
                apiaryId = hive.apiaryId,
                category = RecommendationCategory.COLONY_HEALTH,
                urgencyBucket = UrgencyBucket.WATCHLIST,
                urgencyScore = urgencyScore(UrgencyBucket.WATCHLIST, 10),
                title = s.weakTitle(hive.name),
                shortReason = s.weakShort(),
                longExplanation = s.weakExplanation(daysSince),
                dueAt = null,
                ruleKey = key,
                actionType = ActionType.OPEN_HIVE,
            ),
        )
    }
}

/**
 * Rule 8 — Harvest season prep.
 * In the run-up to harvest, flag strong colonies with full stores so supers /
 * extraction can be planned. Watchlist in the prep month, Due soon once the
 * harvest window is open.
 */
object HarvestPrepRule : HiveRule {
    override val key = "harvest_prep"

    override fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation> {
        val hive = ctx.hive
        val month = ctx.currentMonth
        val prepMonth = Season.isHarvestPrepMonth(month, ctx.seasonal)
        val inWindow = Season.isHarvestWindow(month, ctx.seasonal)
        if (!prepMonth && !inWindow) return emptyList()

        // Only worth surfacing for productive colonies with stores to take.
        val productive = hive.strength == ColonyStrength.STRONG && hive.foodStores == FoodStores.STRONG
        if (!productive) return emptyList()

        val bucket = if (inWindow) UrgencyBucket.DUE_SOON else UrgencyBucket.WATCHLIST
        val s = config.strings

        return listOf(
            Recommendation(
                hiveId = hive.id,
                apiaryId = hive.apiaryId,
                category = RecommendationCategory.HARVEST,
                urgencyBucket = bucket,
                urgencyScore = urgencyScore(bucket, 8),
                title = s.harvestTitle(hive.name),
                shortReason = s.harvestShort(inWindow),
                longExplanation = s.harvestExplanation(inWindow),
                dueAt = null,
                ruleKey = key,
                actionType = ActionType.LOG_HARVEST,
            ),
        )
    }
}
