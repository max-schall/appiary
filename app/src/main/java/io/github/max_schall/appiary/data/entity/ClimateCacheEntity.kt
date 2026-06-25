package io.github.max_schall.appiary.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached climate classification for a location (keyed by rounded lat/lon), so we
 * don't re-fetch Open-Meteo on every engine run and can reuse it offline after
 * the first successful fetch. Monthly normals are stored as CSV for simplicity.
 */
@Entity(tableName = "climate_cache")
data class ClimateCacheEntity(
    @PrimaryKey val locationKey: String,
    val latitude: Double,
    val longitude: Double,
    val koppen: String,
    val koppenGroup: String,
    val hardinessZone: Int,
    val annualMinTempC: Double,
    val monthlyTempsCsv: String,
    val monthlyPrecipCsv: String,
    val fetchedAt: Long,
)
