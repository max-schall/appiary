package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.model.YesNoUnsure
import io.github.max_schall.appiary.domain.rules.HiveContext
import io.github.max_schall.appiary.domain.rules.HiveRule
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.urgencyScore
import io.github.max_schall.appiary.util.TimeUtil

/** Queen not confirmed on a single recent visit (yes/no/unsure). */
private fun InspectionEntity.queenUnconfirmed(): Boolean =
    queenSeen != YesNoUnsure.YES && eggsSeen != YesNoUnsure.YES

/** Count of consecutive most-recent inspections where the queen was unconfirmed. */
private fun consecutiveUnconfirmed(recent: List<InspectionEntity>): Int =
    recent.takeWhile { it.queenUnconfirmed() }.size

/**
 * Rule 2 — Queen uncertainty follow-up.
 * After an inspection where the queen wasn't confirmed (and the colony isn't
 * already in repeated-uncertainty escalation), prompt a focused re-check.
 * Treats an outright queenless reading as Do now.
 */
object QueenUncertaintyRule : HiveRule {
    override val key = "queen_uncertainty"

    override fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation> {
        val hive = ctx.hive
        // Defer to the escalation rule when uncertainty has repeated.
        if (consecutiveUnconfirmed(ctx.recentInspections) >= config.repeatedQueenUncertaintyCount) {
            return emptyList()
        }

        val queenless = hive.queenStatus == QueenStatus.QUEENLESS
        val uncertain = hive.queenStatus == QueenStatus.UNCERTAIN
        if (!queenless && !uncertain) return emptyList()

        val daysSince = hive.lastInspectionAt?.let { TimeUtil.daysBetween(it, ctx.now) } ?: 0
        val bucket = when {
            queenless -> UrgencyBucket.DO_NOW
            daysSince >= config.queenFollowUpDays -> UrgencyBucket.DO_NOW
            else -> UrgencyBucket.DUE_SOON
        }
        val severity = if (queenless) 25 else daysSince.coerceIn(0, 15).toInt()
        val s = config.strings

        return listOf(
            Recommendation(
                hiveId = hive.id,
                apiaryId = hive.apiaryId,
                category = RecommendationCategory.QUEEN,
                urgencyBucket = bucket,
                urgencyScore = urgencyScore(bucket, severity),
                title = s.queenTitle(hive.name, queenless),
                shortReason = s.queenShort(queenless),
                longExplanation = s.queenExplanation(queenless, daysSince, config.queenFollowUpDays),
                dueAt = hive.lastInspectionAt?.plus(TimeUtil.days(config.queenFollowUpDays.toLong())),
                ruleKey = key,
                actionType = ActionType.LOG_INSPECTION,
            ),
        )
    }
}

/**
 * Rule 9 — Repeated queen uncertainty escalation.
 * Two+ consecutive inspections without a confirmed queen point to a real
 * problem (failing/missing queen). Escalates to a decisive action.
 */
object RepeatedQueenUncertaintyRule : HiveRule {
    override val key = "queen_uncertainty_repeated"

    override fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation> {
        val hive = ctx.hive
        val streak = consecutiveUnconfirmed(ctx.recentInspections)
        if (streak < config.repeatedQueenUncertaintyCount) return emptyList()

        val bucket = UrgencyBucket.DO_NOW
        val severity = (10 + streak * 5).coerceIn(0, 30)
        val oldest = ctx.recentInspections.take(streak).lastOrNull()
        val spanDays = oldest?.let { TimeUtil.daysBetween(it.performedAt, ctx.now) } ?: 0
        val s = config.strings

        return listOf(
            Recommendation(
                hiveId = hive.id,
                apiaryId = hive.apiaryId,
                category = RecommendationCategory.QUEEN,
                urgencyBucket = bucket,
                urgencyScore = urgencyScore(bucket, severity),
                title = s.repeatedQueenTitle(hive.name),
                shortReason = s.repeatedQueenShort(streak),
                longExplanation = s.repeatedQueenExplanation(streak, spanDays),
                dueAt = ctx.now,
                ruleKey = key,
                actionType = ActionType.REVIEW,
            ),
        )
    }
}
