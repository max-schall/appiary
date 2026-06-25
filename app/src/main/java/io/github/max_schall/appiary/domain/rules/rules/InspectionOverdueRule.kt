package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.QueenStatus
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
 * Rule 1 — Inspection overdue.
 * Compares days since the last inspection (or since install, if never
 * inspected) against the season-appropriate interval. Surfaces as Due soon when
 * the interval is approaching and Do now once overdue.
 */
object InspectionOverdueRule : HiveRule {
    override val key = "inspection_overdue"

    override fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation> {
        val hive = ctx.hive
        val reference = hive.lastInspectionAt ?: hive.installedAt ?: hive.createdAt
        val daysSince = TimeUtil.daysBetween(reference, ctx.now)
        val interval = if (Season.isActiveSeason(ctx.currentMonth, ctx.seasonal)) {
            config.activeSeasonInspectionIntervalDays
        } else {
            config.offSeasonInspectionIntervalDays
        }
        val daysUntilDue = interval - daysSince
        val neverInspected = hive.lastInspectionAt == null

        // Not due yet and not within the warning window → nothing to say.
        if (daysUntilDue > config.dueSoonWindowDays) return emptyList()

        val overdueBy = daysSince - interval
        // Overdue → act now; merely approaching the interval → due soon.
        val bucket = if (daysSince >= interval) UrgencyBucket.DO_NOW else UrgencyBucket.DUE_SOON

        val severity = overdueBy.coerceIn(0, 30).toInt()
        val overdue = daysSince >= interval
        val s = config.strings

        return listOf(
            Recommendation(
                hiveId = hive.id,
                apiaryId = hive.apiaryId,
                category = RecommendationCategory.INSPECTION,
                urgencyBucket = bucket,
                urgencyScore = urgencyScore(bucket, severity),
                title = s.inspectTitle(hive.name),
                shortReason = s.inspectShort(overdue, if (overdue) overdueBy.coerceAtLeast(0) else daysUntilDue),
                longExplanation = s.inspectExplanation(
                    neverInspected, daysSince, interval, hive.queenStatus == QueenStatus.UNCERTAIN,
                ),
                dueAt = reference + TimeUtil.days(interval.toLong()),
                ruleKey = key,
                actionType = ActionType.LOG_INSPECTION,
            ),
        )
    }
}
