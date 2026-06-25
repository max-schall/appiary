package io.github.max_schall.appiary.data.dao

/** Apiary row enriched with derived counts for the Apiaries list. */
data class ApiaryStats(
    val id: String,
    val name: String,
    val hiveCount: Int,
    val openRecommendationCount: Int,
    val lastVisitAt: Long?,
)
