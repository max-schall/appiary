package io.github.max_schall.appiary.ui.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.RecommendationRepository
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.ui.components.CounterData
import io.github.max_schall.appiary.ui.model.groupRecommendations
import io.github.max_schall.appiary.ui.state.ApiaryOption
import io.github.max_schall.appiary.ui.state.TodayUiState
import io.github.max_schall.appiary.util.TimeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayViewModel(
    private val recommendationRepo: RecommendationRepository,
    apiaryRepo: ApiaryRepository,
    hiveRepo: HiveRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val selectedApiaryId = MutableStateFlow<String?>(null)
    private val selectedBucket = MutableStateFlow<UrgencyBucket?>(null)

    val uiState = combine(
        recommendationRepo.observeActive(),
        hiveRepo.observeAll(),
        apiaryRepo.observeApiaries(),
        selectedApiaryId,
        selectedBucket,
    ) { recs, hives, apiaries, apiaryId, bucket ->
        val hiveNames = hives.associate { it.id to it.name }
        val apiaryNames = apiaries.associate { it.id to it.name }

        val scopedRecs = recs.filter { apiaryId == null || it.apiaryId == apiaryId }
        val scopedHives = hives.filter { apiaryId == null || it.apiaryId == apiaryId }

        val hivesWithRecs = scopedRecs.mapNotNull { it.hiveId }.toSet()
        val counts = CounterData(
            doNow = scopedRecs.count { it.urgencyBucket == UrgencyBucket.DO_NOW },
            dueSoon = scopedRecs.count { it.urgencyBucket == UrgencyBucket.DUE_SOON },
            watchlist = scopedRecs.count { it.urgencyBucket == UrgencyBucket.WATCHLIST },
            healthy = scopedHives.count { it.id !in hivesWithRecs },
        )

        val groups = groupRecommendations(
            recs = scopedRecs.filter { bucket == null || it.urgencyBucket == bucket },
            hiveNames = hiveNames,
            apiaryNames = apiaryNames,
        )

        TodayUiState(
            loading = false,
            counts = counts,
            groups = groups,
            apiaries = apiaries.map { ApiaryOption(it.id, it.name) },
            selectedApiaryId = apiaryId,
            selectedBucket = bucket,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun setApiaryFilter(id: String?) { selectedApiaryId.value = id }
    fun toggleBucketFilter(bucket: UrgencyBucket?) { selectedBucket.value = bucket }

    fun complete(rec: GeneratedRecommendationEntity) =
        viewModelScope.launch { recommendationRepo.complete(rec.id) }

    fun dismiss(rec: GeneratedRecommendationEntity) =
        viewModelScope.launch { recommendationRepo.dismiss(rec.id) }

    fun snooze(rec: GeneratedRecommendationEntity, days: Long = 3) =
        viewModelScope.launch { recommendationRepo.snooze(rec.id, clock() + TimeUtil.days(days)) }
}
