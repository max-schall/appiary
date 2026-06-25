package io.github.max_schall.appiary.data.climate

import io.github.max_schall.appiary.data.dao.ClimateCacheDao
import io.github.max_schall.appiary.data.entity.ClimateCacheEntity
import io.github.max_schall.appiary.domain.climate.ClimateEstimator
import io.github.max_schall.appiary.domain.climate.SeasonalEstimate
import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.season.ClimateClassifier
import io.github.max_schall.appiary.domain.season.ClimateProfile
import io.github.max_schall.appiary.domain.season.KoppenGroup
import java.util.concurrent.TimeUnit

/** Estimate (offline) or null if it came from refined online data. */
data class SeasonResult(val estimate: SeasonalEstimate, val usedOnlineData: Boolean)

/**
 * Produces a seasonal estimate + climate classification for a location: refines
 * with Open-Meteo normals when reachable, caches the result so later runs work
 * offline, and falls back to the offline latitude heuristic otherwise.
 */
class ClimateRepository(
    private val client: OpenMeteoClient = OpenMeteoClient(),
    private val cacheDao: ClimateCacheDao? = null,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val freshnessMs = TimeUnit.DAYS.toMillis(180)

    suspend fun estimateSeason(latitude: Double, longitude: Double): SeasonResult {
        val temps = client.monthlyMeanTemps(latitude, longitude)
        return if (temps != null) {
            SeasonResult(ClimateEstimator.fromMonthlyTemps(latitude, temps), usedOnlineData = true)
        } else {
            SeasonResult(ClimateEstimator.fromLatitude(latitude), usedOnlineData = false)
        }
    }

    private fun hemisphereOf(lat: Double) = if (lat < 0) Hemisphere.SOUTHERN else Hemisphere.NORTHERN
    private fun keyOf(lat: Double, lon: Double) = "%.2f,%.2f".format(java.util.Locale.US, lat, lon)

    /**
     * Climate descriptor for the location. Returns a fresh cached value if
     * available; otherwise fetches + classifies + caches; otherwise reuses any
     * stale cache; otherwise null (no network and never fetched here).
     */
    suspend fun climateProfile(latitude: Double, longitude: Double): ClimateProfile? {
        val key = keyOf(latitude, longitude)
        val cached = cacheDao?.get(key)
        if (cached != null && clock() - cached.fetchedAt < freshnessMs) {
            return cached.toProfile(latitude)
        }
        val normals = client.monthlyNormals(latitude, longitude)
            ?: return cached?.toProfile(latitude) // offline → reuse stale if present
        val profile = ClimateClassifier.profile(
            normals.tempC, normals.precipMm, normals.annualMinTempC, hemisphereOf(latitude),
        )
        cacheDao?.upsert(
            ClimateCacheEntity(
                locationKey = key, latitude = latitude, longitude = longitude,
                koppen = profile.code, koppenGroup = profile.group.name,
                hardinessZone = profile.hardinessZone, annualMinTempC = normals.annualMinTempC,
                monthlyTempsCsv = normals.tempC.joinToString(","),
                monthlyPrecipCsv = normals.precipMm.joinToString(","),
                fetchedAt = clock(),
            ),
        )
        return profile
    }

    private fun ClimateCacheEntity.toProfile(lat: Double): ClimateProfile = ClimateProfile(
        code = koppen,
        group = runCatching { KoppenGroup.valueOf(koppenGroup) }.getOrDefault(KoppenGroup.TEMPERATE),
        hardinessZone = hardinessZone,
        winterSeverity = ClimateClassifier.winterSeverity(hardinessZone),
        hemisphere = hemisphereOf(lat),
    )
}
