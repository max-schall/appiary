package io.github.max_schall.appiary.ui.model

import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.data.entity.HiveEntity

/** A hive row enriched with its apiary name and most-urgent open action. */
data class HiveListUi(
    val hive: HiveEntity,
    val apiaryName: String?,
    val nextAction: GeneratedRecommendationEntity?,
)
