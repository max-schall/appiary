package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.TaskStatus
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.urgencyScore
import io.github.max_schall.appiary.util.TimeUtil

/**
 * Rule 10 — Overdue manual follow-up.
 * Surfaces the beekeeper's own open tasks once their due date passes (or is
 * within the warning window), so manual reminders sit alongside generated ones.
 * Operates over the task list rather than a single hive.
 */
object ManualFollowUpRule {
    const val KEY = "manual_overdue"

    fun evaluate(tasks: List<ManualTaskEntity>, config: RuleConfig, now: Long): List<Recommendation> =
        tasks.asSequence()
            .filter { it.status == TaskStatus.OPEN }
            .mapNotNull { task ->
                val dueAt = task.dueAt ?: return@mapNotNull null
                val daysUntil = TimeUtil.daysBetween(now, dueAt)
                if (daysUntil > config.dueSoonWindowDays) return@mapNotNull null

                val overdue = daysUntil < 0
                val bucket = if (daysUntil <= 0) UrgencyBucket.DO_NOW else UrgencyBucket.DUE_SOON
                val severity = (-daysUntil).coerceIn(0, 30).toInt()
                val s = config.strings

                Recommendation(
                    hiveId = task.hiveId,
                    apiaryId = task.apiaryId,
                    category = RecommendationCategory.MANUAL,
                    urgencyBucket = bucket,
                    urgencyScore = urgencyScore(bucket, severity),
                    title = task.title,
                    shortReason = s.manualShort(overdue, daysUntil),
                    longExplanation = s.manualExplanation(overdue, daysUntil, task.details),
                    // Per-task uniqueness is encoded in the rule key so multiple
                    // overdue tasks don't collide during reconciliation.
                    dueAt = dueAt,
                    ruleKey = "$KEY:${task.id}",
                    actionType = ActionType.COMPLETE_TASK,
                )
            }
            .toList()
}
