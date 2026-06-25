package io.github.max_schall.appiary.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** An apiary that has a geolocated site — one map marker. */
data class LocatedApiary(
    val apiaryId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

class MapViewModel(apiaryRepo: ApiaryRepository) : ViewModel() {

    val located = combine(
        apiaryRepo.observeApiaries(),
        apiaryRepo.observeSites(),
    ) { apiaries, sites ->
        val sitesById = sites.associateBy { it.id }
        apiaries.mapNotNull { apiary ->
            val site = apiary.siteId?.let(sitesById::get) ?: return@mapNotNull null
            val lat = site.latitude ?: return@mapNotNull null
            val lng = site.longitude ?: return@mapNotNull null
            LocatedApiary(apiary.id, apiary.name, lat, lng)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
