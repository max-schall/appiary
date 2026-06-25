package io.github.max_schall.appiary.domain.rules.rules

import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.rules.ApiaryContext
import io.github.max_schall.appiary.domain.rules.ApiaryRule
import io.github.max_schall.appiary.domain.rules.Recommendation
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.urgencyScore

/**
 * German Bestandsbuch compliance (EU 2019/6 Art. 108): for apiaries in Germany,
 * flags treatments that still lack a proof-of-purchase receipt so the record can
 * be made complete and traceable. Opens the record-book screen.
 */
object BestandsbuchRule : ApiaryRule {
    override val key = "bestandsbuch"

    override fun evaluate(ctx: ApiaryContext, config: RuleConfig): List<Recommendation> {
        if (ctx.countryCode != "DE" || ctx.treatmentsMissingReceipt <= 0) return emptyList()
        val s = config.strings
        return listOf(
            Recommendation(
                hiveId = null,
                apiaryId = ctx.apiaryId,
                category = RecommendationCategory.COMPLIANCE,
                urgencyBucket = UrgencyBucket.DUE_SOON,
                urgencyScore = urgencyScore(UrgencyBucket.DUE_SOON, 18),
                title = s.bestandsbuchTitle(),
                shortReason = s.bestandsbuchShort(ctx.treatmentsMissingReceipt),
                longExplanation = s.bestandsbuchExplanation(ctx.treatmentsMissingReceipt),
                dueAt = null,
                ruleKey = "$key:${ctx.apiaryId}",
                actionType = ActionType.REVIEW,
            ),
        )
    }
}
