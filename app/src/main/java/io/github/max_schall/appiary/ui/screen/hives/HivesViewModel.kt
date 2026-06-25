package io.github.max_schall.appiary.ui.screen.hives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.RecommendationRepository
import io.github.max_schall.appiary.domain.rules.rank
import io.github.max_schall.appiary.ui.model.HiveListUi
import io.github.max_schall.appiary.ui.state.ApiaryOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HivesUiState(
    val hives: List<HiveListUi> = emptyList(),
    val apiaries: List<ApiaryOption> = emptyList(),
    val query: String = "",
    val selectedApiaryId: String? = null,
)

class HivesViewModel(
    hiveRepo: HiveRepository,
    apiaryRepo: ApiaryRepository,
    recommendationRepo: RecommendationRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedApiaryId = MutableStateFlow<String?>(null)

    private fun topActionFor(hiveId: String, recs: List<GeneratedRecommendationEntity>) =
        recs.filter { it.hiveId == hiveId }
            .minWithOrNull(compareBy({ it.urgencyBucket.rank }, { -it.urgencyScore }))

    val uiState = combine(
        hiveRepo.observeAll(),
        apiaryRepo.observeApiaries(),
        recommendationRepo.observeActive(),
        query,
        selectedApiaryId,
    ) { hives, apiaries, recs, q, apiaryId ->
        val apiaryNames = apiaries.associate { it.id to it.name }
        val filtered = hives
            .filter { apiaryId == null || it.apiaryId == apiaryId }
            .filter { q.isBlank() || it.name.contains(q, ignoreCase = true) }
            .map { HiveListUi(it, apiaryNames[it.apiaryId], topActionFor(it.id, recs)) }

        HivesUiState(
            hives = filtered,
            apiaries = apiaries.map { ApiaryOption(it.id, it.name) },
            query = q,
            selectedApiaryId = apiaryId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HivesUiState())

    fun setQuery(q: String) { query.value = q }
    fun setApiaryFilter(id: String?) { selectedApiaryId.value = id }
}
