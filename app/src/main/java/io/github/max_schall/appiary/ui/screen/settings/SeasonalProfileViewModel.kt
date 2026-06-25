package io.github.max_schall.appiary.ui.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.climate.ClimateRepository
import io.github.max_schall.appiary.data.dao.SeasonalProfileDao
import io.github.max_schall.appiary.data.entity.SeasonalProfileEntity
import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.season.ClimateProfile
import io.github.max_schall.appiary.domain.season.PhenologyEngine
import io.github.max_schall.appiary.domain.season.RegionResolver
import io.github.max_schall.appiary.domain.season.SeasonModel
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.util.CalendarUtil
import io.github.max_schall.appiary.util.LocationProvider
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SeasonalProfileForm(
    val id: String? = null,
    val name: String = "",
    val hemisphere: Hemisphere = Hemisphere.NORTHERN,
    val activeStart: Int = 3,
    val activeEnd: Int = 9,
    val harvestStart: Int = 7,
    val harvestEnd: Int = 9,
    val winterPrep: Int = 9,
    val latitude: String = "",
    val longitude: String = "",
)

class SeasonalProfileViewModel(
    application: Application,
    private val dao: SeasonalProfileDao,
    private val climateRepo: ClimateRepository,
    private val refreshRecommendations: RefreshRecommendations,
    private val clock: () -> Long = System::currentTimeMillis,
) : AndroidViewModel(application) {

    private val _form = MutableStateFlow(SeasonalProfileForm())
    val form = _form.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status = _status.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    private val _climate = MutableStateFlow<ClimateProfile?>(null)
    val climate = _climate.asStateFlow()

    private val _seasonModel = MutableStateFlow<SeasonModel?>(null)
    val seasonModel = _seasonModel.asStateFlow()

    private fun recomputeSeasonModel() {
        val f = _form.value
        val climate = _climate.value
        val profile = SeasonalProfileEntity(
            id = f.id ?: "", name = f.name, hemisphere = f.hemisphere,
            activeSeasonStartMonth = f.activeStart, activeSeasonEndMonth = f.activeEnd,
            harvestStartMonth = f.harvestStart, harvestEndMonth = f.harvestEnd,
            winterPrepMonth = f.winterPrep, createdAt = 0, updatedAt = 0,
        )
        val lat = f.latitude.toDoubleOrNull()
        val lon = f.longitude.toDoubleOrNull()
        val calendar = RegionResolver.resolve(lat, lon, climate?.group)
        _seasonModel.value = PhenologyEngine.model(CalendarUtil.monthOf(clock()), profile, climate, calendar)
    }

    init {
        viewModelScope.launch {
            dao.getSelected()?.let { p ->
                _form.value = SeasonalProfileForm(
                    id = p.id, name = p.name, hemisphere = p.hemisphere,
                    activeStart = p.activeSeasonStartMonth, activeEnd = p.activeSeasonEndMonth,
                    harvestStart = p.harvestStartMonth, harvestEnd = p.harvestEndMonth,
                    winterPrep = p.winterPrepMonth,
                    latitude = p.latitude?.toString() ?: "", longitude = p.longitude?.toString() ?: "",
                )
                // Show cached climate for a saved location without a network call.
                val lat = p.latitude; val lon = p.longitude
                if (lat != null && lon != null) _climate.value = climateRepo.climateProfile(lat, lon)
            }
            recomputeSeasonModel()
        }
    }

    fun update(transform: (SeasonalProfileForm) -> SeasonalProfileForm) { _form.value = transform(_form.value) }

    private fun s(resId: Int) = getApplication<Application>().getString(resId)

    /** Fill lat/lon from the device's last-known location (offline-capable). */
    fun useDeviceLocation() {
        val ll = LocationProvider.lastKnown(getApplication())
        if (ll == null) {
            _status.value = s(R.string.season_location_unavailable)
        } else {
            _form.value = _form.value.copy(latitude = format(ll.first), longitude = format(ll.second))
            _status.value = null
        }
    }

    /** Derive the season from the entered coordinates (online refine, offline fallback). */
    fun deriveFromLocation() {
        val lat = _form.value.latitude.toDoubleOrNull()
        val lon = _form.value.longitude.toDoubleOrNull() ?: lat // longitude only matters online
        if (lat == null || lon == null) {
            _status.value = s(R.string.season_location_unavailable)
            return
        }
        viewModelScope.launch {
            _busy.value = true
            val result = climateRepo.estimateSeason(lat, lon)
            val e = result.estimate
            _form.value = _form.value.copy(
                hemisphere = e.hemisphere,
                activeStart = e.activeStartMonth, activeEnd = e.activeEndMonth,
                harvestStart = e.harvestStartMonth, harvestEnd = e.harvestEndMonth,
                winterPrep = e.winterPrepMonth,
            )
            _status.value = s(if (result.usedOnlineData) R.string.season_derived_online else R.string.season_derived_offline)
            _climate.value = climateRepo.climateProfile(lat, lon)
            recomputeSeasonModel()
            _busy.value = false
        }
    }

    fun save() {
        val f = _form.value
        val now = clock()
        viewModelScope.launch {
            dao.upsert(
                SeasonalProfileEntity(
                    id = f.id ?: newId(),
                    name = f.name.ifBlank { "Season" },
                    hemisphere = f.hemisphere,
                    activeSeasonStartMonth = f.activeStart, activeSeasonEndMonth = f.activeEnd,
                    harvestStartMonth = f.harvestStart, harvestEndMonth = f.harvestEnd,
                    winterPrepMonth = f.winterPrep,
                    latitude = f.latitude.toDoubleOrNull(), longitude = f.longitude.toDoubleOrNull(),
                    selected = true,
                    createdAt = now, updatedAt = now,
                ),
            )
            refreshRecommendations() // season affects inspection cadence & harvest rules
            _saved.value = true
        }
    }

    private fun format(v: Double) = String.format(java.util.Locale.US, "%.4f", v)
}
