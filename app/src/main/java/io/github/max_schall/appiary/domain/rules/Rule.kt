package io.github.max_schall.appiary.domain.rules

import io.github.max_schall.appiary.domain.model.UrgencyBucket

/** A deterministic rule that reads one hive's context and emits zero+ recs. */
interface HiveRule {
    val key: String
    fun evaluate(ctx: HiveContext, config: RuleConfig): List<Recommendation>
}

/** A deterministic rule that reads one apiary's (location/season) context. */
interface ApiaryRule {
    val key: String
    fun evaluate(ctx: ApiaryContext, config: RuleConfig): List<Recommendation>
}

/** Sort priority for buckets: Do now first, Healthy last. */
val UrgencyBucket.rank: Int
    get() = when (this) {
        UrgencyBucket.DO_NOW -> 0
        UrgencyBucket.DUE_SOON -> 1
        UrgencyBucket.WATCHLIST -> 2
        UrgencyBucket.HEALTHY -> 3
    }

/**
 * Combined urgency score (0..100) used to order items within and across
 * buckets. Each bucket occupies a band; [severity] (0..30) ranks items inside
 * it. Keeping scoring central makes ordering predictable and testable.
 */
fun urgencyScore(bucket: UrgencyBucket, severity: Int): Int {
    val base = when (bucket) {
        UrgencyBucket.DO_NOW -> 70
        UrgencyBucket.DUE_SOON -> 40
        UrgencyBucket.WATCHLIST -> 15
        UrgencyBucket.HEALTHY -> 0
    }
    return base + severity.coerceIn(0, 30)
}
