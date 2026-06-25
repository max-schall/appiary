package io.github.max_schall.appiary.domain.rules

import io.github.max_schall.appiary.domain.rules.rules.HarvestPrepRule
import io.github.max_schall.appiary.domain.rules.rules.InspectionOverdueRule
import io.github.max_schall.appiary.domain.rules.rules.LowStoresRule
import io.github.max_schall.appiary.domain.rules.rules.ManualFollowUpRule
import io.github.max_schall.appiary.domain.rules.rules.MiteCheckOverdueRule
import io.github.max_schall.appiary.domain.rules.rules.PostTreatmentCheckRule
import io.github.max_schall.appiary.domain.rules.rules.BestandsbuchRule
import io.github.max_schall.appiary.domain.rules.rules.ColdSnapRule
import io.github.max_schall.appiary.domain.rules.rules.DearthRule
import io.github.max_schall.appiary.domain.rules.rules.InspectionWeatherRule
import io.github.max_schall.appiary.domain.rules.rules.NectarFlowRule
import io.github.max_schall.appiary.domain.rules.rules.QueenUncertaintyRule
import io.github.max_schall.appiary.domain.rules.rules.RepeatedQueenUncertaintyRule
import io.github.max_schall.appiary.domain.rules.rules.SeasonalTaskRule
import io.github.max_schall.appiary.domain.rules.rules.SwarmRiskRule
import io.github.max_schall.appiary.domain.rules.rules.TreatmentWeatherRule
import io.github.max_schall.appiary.domain.rules.rules.WeakColonyRule

/**
 * Deterministic recommendation engine. Runs every hive rule against every hive
 * context plus the manual-task rule, then returns recommendations ordered by
 * urgency (bucket, then score). No randomness, no I/O, no LLM — same inputs
 * always yield the same output, which is what makes recommendations explainable
 * and testable.
 */
class RuleEngine(
    private val config: RuleConfig = RuleConfig.DEFAULT,
    private val hiveRules: List<HiveRule> = DEFAULT_HIVE_RULES,
    private val apiaryRules: List<ApiaryRule> = DEFAULT_APIARY_RULES,
) {
    /** Recommendations for a single hive (used by Hive detail + tests). */
    fun evaluateHive(ctx: HiveContext): List<Recommendation> =
        hiveRules.flatMap { it.evaluate(ctx, config) }

    /** Full run across all hives, apiaries, and manual tasks, sorted most-urgent first. */
    fun evaluate(input: EvaluationInput): List<Recommendation> {
        val hiveRecs = input.hiveContexts.flatMap { evaluateHive(it) }
        val apiaryRecs = input.apiaryContexts.flatMap { ctx -> apiaryRules.flatMap { it.evaluate(ctx, config) } }
        val taskRecs = ManualFollowUpRule.evaluate(input.openTasks, config, input.now)
        return (hiveRecs + apiaryRecs + taskRecs).sortedWith(URGENCY_ORDER)
    }

    companion object {
        val DEFAULT_HIVE_RULES: List<HiveRule> = listOf(
            InspectionOverdueRule,
            QueenUncertaintyRule,
            RepeatedQueenUncertaintyRule,
            SwarmRiskRule,
            LowStoresRule,
            MiteCheckOverdueRule,
            PostTreatmentCheckRule,
            WeakColonyRule,
            HarvestPrepRule,
        )

        val DEFAULT_APIARY_RULES: List<ApiaryRule> = listOf(
            SeasonalTaskRule,
            NectarFlowRule,
            DearthRule,
            InspectionWeatherRule,
            TreatmentWeatherRule,
            ColdSnapRule,
            BestandsbuchRule,
        )

        /** Bucket rank first, then higher score, then stable by title. */
        val URGENCY_ORDER: Comparator<Recommendation> =
            compareBy<Recommendation> { it.urgencyBucket.rank }
                .thenByDescending { it.urgencyScore }
                .thenBy { it.title }
    }
}
