package io.github.max_schall.appiary.ui.model

import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.domain.rules.rank

/** Most-urgent first: bucket rank, then higher score, then title (stable). */
private val REC_ORDER = compareBy<GeneratedRecommendationEntity> { it.urgencyBucket.rank }
    .thenByDescending { it.urgencyScore }
    .thenBy { it.title }

/**
 * Collapse a flat recommendation list into one group per hive (its colony items)
 * plus one group per apiary for location-level items (hiveId == null), and a
 * catch-all GENERAL group for anything unscoped. Groups are ordered most-urgent
 * first; within a tie, hive cards precede apiary/general cards.
 */
fun groupRecommendations(
    recs: List<GeneratedRecommendationEntity>,
    hiveNames: Map<String, String>,
    apiaryNames: Map<String, String>,
): List<RecommendationGroup> {
    fun item(rec: GeneratedRecommendationEntity) = RecommendationItem(
        rec = rec,
        hiveName = rec.hiveId?.let(hiveNames::get),
        apiaryName = rec.apiaryId?.let(apiaryNames::get),
    )
    fun worst(group: List<GeneratedRecommendationEntity>) =
        group.minByOrNull { it.urgencyBucket.rank }!!.urgencyBucket

    val hiveGroups = recs.filter { it.hiveId != null }
        .groupBy { it.hiveId!! }
        .map { (hiveId, group) ->
            val sorted = group.sortedWith(REC_ORDER)
            RecommendationGroup(
                key = "hive:$hiveId",
                kind = GroupKind.HIVE,
                title = hiveNames[hiveId],
                subtitle = sorted.first().apiaryId?.let(apiaryNames::get),
                worstBucket = worst(group),
                items = sorted.map(::item),
            )
        }

    val locationGroups = recs.filter { it.hiveId == null }
        .groupBy { it.apiaryId }
        .map { (apiaryId, group) ->
            val sorted = group.sortedWith(REC_ORDER)
            RecommendationGroup(
                key = "apiary:${apiaryId ?: "general"}",
                kind = if (apiaryId == null) GroupKind.GENERAL else GroupKind.APIARY,
                title = apiaryId?.let(apiaryNames::get),
                subtitle = null,
                worstBucket = worst(group),
                items = sorted.map(::item),
            )
        }

    return (hiveGroups + locationGroups).sortedWith(
        compareBy<RecommendationGroup> { it.worstBucket.rank }
            .thenBy { it.kind.ordinal }
            .thenBy { it.title ?: "" },
    )
}
