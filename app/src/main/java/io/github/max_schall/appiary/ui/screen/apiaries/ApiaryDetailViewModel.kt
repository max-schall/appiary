package io.github.max_schall.appiary.ui.screen.apiaries

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.ColonyRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.RecommendationRepository
import io.github.max_schall.appiary.domain.rules.rank
import io.github.max_schall.appiary.ui.model.HiveListUi
import io.github.max_schall.appiary.ui.model.RecommendationGroup
import io.github.max_schall.appiary.ui.model.groupRecommendations
import io.github.max_schall.appiary.util.CountryResolver
import io.github.max_schall.appiary.util.LocationProvider
import io.github.max_schall.appiary.util.TimeUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ApiaryDetailUiState(
    val apiaryName: String = "",
    val hives: List<HiveListUi> = emptyList(),
    val openItems: List<RecommendationGroup> = emptyList(),
)

class ApiaryDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val apiaryRepo: ApiaryRepository,
    private val hiveRepo: HiveRepository,
    private val recommendationRepo: RecommendationRepository,
    private val colonyRepo: ColonyRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : AndroidViewModel(application) {

    val apiaryId: String = checkNotNull(savedStateHandle["apiaryId"])

    fun addHive(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { hiveRepo.createHive(apiaryId = apiaryId, name = name.trim()) }
    }

    fun captureSwarm(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { colonyRepo.captureSwarm(apiaryId = apiaryId, name = name.trim()) }
    }

    /** Last-known device coordinates, or null if unavailable / permission denied. */
    fun currentLocation(): Pair<Double, Double>? = LocationProvider.lastKnown(getApplication())

    /** Persist this apiary's location (resolving its country for record-keeping). */
    fun setLocation(latitude: Double, longitude: Double) = viewModelScope.launch {
        val country = CountryResolver.resolve(getApplication(), latitude, longitude)
        apiaryRepo.setLocation(apiaryId, latitude, longitude, country)
    }

    fun complete(rec: GeneratedRecommendationEntity) =
        viewModelScope.launch { recommendationRepo.complete(rec.id) }

    fun dismiss(rec: GeneratedRecommendationEntity) =
        viewModelScope.launch { recommendationRepo.dismiss(rec.id) }

    fun snooze(rec: GeneratedRecommendationEntity, days: Long = 3) =
        viewModelScope.launch { recommendationRepo.snooze(rec.id, clock() + TimeUtil.days(days)) }

    private fun topActionFor(hiveId: String, recs: List<GeneratedRecommendationEntity>) =
        recs.filter { it.hiveId == hiveId }
            .minWithOrNull(compareBy({ it.urgencyBucket.rank }, { -it.urgencyScore }))

    val uiState = combine(
        apiaryRepo.observeApiary(apiaryId),
        hiveRepo.observeByApiary(apiaryId),
        recommendationRepo.observeActiveByApiary(apiaryId),
    ) { apiary, hives, recs ->
        ApiaryDetailUiState(
            apiaryName = apiary?.name ?: "Apiary",
            hives = hives.map { HiveListUi(it, apiary?.name, topActionFor(it.id, recs)) },
            openItems = groupRecommendations(
                recs = recs,
                hiveNames = hives.associate { it.id to it.name },
                apiaryNames = apiary?.let { mapOf(it.id to it.name) } ?: emptyMap(),
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApiaryDetailUiState())
}
