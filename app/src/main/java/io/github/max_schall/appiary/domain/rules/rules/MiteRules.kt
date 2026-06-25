package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.TreatmentState
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.HiveContext
import io.github.max_schall.appiary.domain.rules.HiveRule
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.Season
import io.github.max_schall.appiary.domain.rules.urgencyScore
import io.github.max_schall.appiary.util.TimeUtil

/**
 * Rule 5 — Mite check overdue.
 * During the active season, varroa should be monitored on a cadence. Overdue
 * checks escalate, and a previous High/Critical reading raises urgency further.
 */
object MiteCheckOverdueRule : HiveRule {
    override val key = "mite_check_overdue"

    override fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation> {
        // Off-season monitoring is lower value; the post-treatment rule still fires.
        if (!Season.isActiveSeason(ctx.currentMonth, ctx.seasonal)) return emptyList()

        val hive = ctx.hive
        val reference = hive.lastMiteCheckAt ?: hive.installedAt ?: hive.createdAt
        val daysSince = TimeUtil.daysBetween(reference, ctx.now)
        val interval = config.miteCheckIntervalDays
        if (daysSince < interval) return emptyList()

        val overdueBy = daysSince - interval
        val lastResult = ctx.latestMiteCheck?.result
        val wasHigh = lastResult == MiteResult.HIGH || lastResult == MiteResult.CRITICAL
        // Very overdue, or previously high → Do now; otherwise Due soon.
        val bucket = if (overdueBy >= interval || wasHigh) UrgencyBucket.DO_NOW else UrgencyBucket.DUE_SOON
        val severity = (overdueBy / 2 + if (wasHigh) 15 else 0).coerceIn(0, 30).toInt()
        val never = hive.lastMiteCheckAt == null
        val s = config.strings

        return listOf(
            Recommendation(
                hiveId = hive.id,
                apiaryId = hive.apiaryId,
                category = RecommendationCategory.MITE_CHECK,
                urgencyBucket = bucket,
                urgencyScore = urgencyScore(bucket, severity),
                title = s.miteCheckTitle(hive.name),
                shortReason = s.miteOverdueShort(never, overdueBy),
                longExplanation = s.miteOverdueExplanation(never, daysSince, interval, if (wasHigh) lastResult else null),
                dueAt = reference + TimeUtil.days(interval.toLong()),
                ruleKey = key,
                actionType = ActionType.LOG_MITE_CHECK,
            ),
        )
    }
}

/**
 * Rule 6 — Post-treatment mite check due.
 * After a treatment ends, an efficacy check confirms it worked. Uses the
 * follow-up date captured on the hive when the treatment was logged.
 */
object PostTreatmentCheckRule : HiveRule {
    override val key = "post_treatment_check"

    override fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation> {
        val hive = ctx.hive
        val dueAt = hive.postTreatmentCheckDueAt ?: return emptyList()
        if (hive.treatmentState != TreatmentState.FOLLOW_UP_DUE) return emptyList()

        val daysUntil = TimeUtil.daysBetween(ctx.now, dueAt)
        if (daysUntil > config.dueSoonWindowDays) return emptyList()

        val bucket = if (daysUntil <= 0) UrgencyBucket.DO_NOW else UrgencyBucket.DUE_SOON
        val severity = (-daysUntil).coerceIn(0, 30).toInt()

        val endedDays = hive.lastTreatmentEndedAt?.let { TimeUtil.daysBetween(it, ctx.now) }
        val s = config.strings

        return listOf(
            Recommendation(
                hiveId = hive.id,
                apiaryId = hive.apiaryId,
                category = RecommendationCategory.MITE_CHECK,
                urgencyBucket = bucket,
                urgencyScore = urgencyScore(bucket, severity),
                title = s.postTreatmentTitle(hive.name),
                shortReason = s.postTreatmentShort(daysUntil),
                longExplanation = s.postTreatmentExplanation(daysUntil, endedDays),
                dueAt = dueAt,
                ruleKey = key,
                actionType = ActionType.LOG_MITE_CHECK,
            ),
        )
    }
}
