package io.github.max_schall.appiary.ui.model

import io.github.max_schall.appiary.domain.model.UrgencyBucket

/** What a [RecommendationGroup] card represents — drives its header icon/label. */
enum class GroupKind { HIVE, APIARY, GENERAL }

/**
 * A set of recommendations consolidated under one card: all the open items for a
 * single hive, or all the apiary-level (weather/season/nectar/compliance) items
 * for one location. [worstBucket] is the most urgent item in the group and drives
 * the card's accent + header badge; [items] are pre-sorted most-urgent first.
 */
data class RecommendationGroup(
    val key: String,
    val kind: GroupKind,
    /** Resolved hive or apiary name; null for the catch-all GENERAL group. */
    val title: String?,
    /** Secondary line — the apiary name for hive groups; null otherwise. */
    val subtitle: String?,
    val worstBucket: UrgencyBucket,
    val items: List<RecommendationItem>,
)
