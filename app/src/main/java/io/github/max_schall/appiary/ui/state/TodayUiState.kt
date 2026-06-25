package io.github.max_schall.appiary.ui.state

import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.ui.components.CounterData
import io.github.max_schall.appiary.ui.model.RecommendationGroup

/** A selectable apiary in the Today/Hives filters. */
data class ApiaryOption(val id: String, val name: String)

data class TodayUiState(
    val loading: Boolean = true,
    val counts: CounterData = CounterData(0, 0, 0, 0),
    /** One card per hive (its colony items) and per apiary (location-level items). */
    val groups: List<RecommendationGroup> = emptyList(),
    val apiaries: List<ApiaryOption> = emptyList(),
    val selectedApiaryId: String? = null,
    val selectedBucket: UrgencyBucket? = null,
)
