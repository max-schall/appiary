package io.github.max_schall.appiary.ui.model

import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity

/** A recommendation row enriched with resolved hive/apiary names for display. */
data class RecommendationItem(
    val rec: GeneratedRecommendationEntity,
    val hiveName: String?,
    val apiaryName: String?,
)
