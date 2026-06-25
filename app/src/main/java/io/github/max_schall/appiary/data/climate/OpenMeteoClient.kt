package io.github.max_schall.appiary.data.climate

import io.github.max_schall.appiary.domain.weather.WeatherDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

/** Monthly climate normals for a location (index 0 = January). */
data class MonthlyNormals(
    val tempC: List<Double>,
    val precipMm: List<Double>,
    val annualMinTempC: Double,
)

/**
 * Fetches climate data for a location from Open-Meteo's free, key-less archive
 * API. Used only to *refine* the offline estimate; every failure path (no
 * network, timeout, bad data) returns null so callers fall back to the latitude
 * heuristic. Nothing here is required for the app to work.
 */
class OpenMeteoClient(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    @Serializable
    private data class ArchiveResponse(val daily: Daily? = null)

    @Serializable
    private data class Daily(
        val time: List<String> = emptyList(),
        @SerialName("temperature_2m_mean") val mean: List<Double?> = emptyList(),
        @SerialName("temperature_2m_min") val min: List<Double?> = emptyList(),
        @SerialName("precipitation_sum") val precip: List<Double?> = emptyList(),
    )

    /** 12 monthly mean temperatures over the last full calendar year, or null. */
    suspend fun monthlyMeanTemps(latitude: Double, longitude: Double): List<Double>? =
        monthlyNormals(latitude, longitude)?.tempC

    /** Monthly temp + precip normals and the annual extreme minimum, or null. */
    suspend fun monthlyNormals(latitude: Double, longitude: Double): MonthlyNormals? =
        withContext(Dispatchers.IO) {
            runCatching {
                val year = Calendar.getInstance().get(Calendar.YEAR) - 1
                val url = URL(
                    "https://archive-api.open-meteo.com/v1/archive" +
                        "?latitude=$latitude&longitude=$longitude" +
                        "&start_date=$year-01-01&end_date=$year-12-31" +
                        "&daily=temperature_2m_mean,temperature_2m_min,precipitation_sum&timezone=auto",
                )
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    requestMethod = "GET"
                }
                val body = conn.use { c ->
                    if (c.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                    c.inputStream.bufferedReader().use { it.readText() }
                }
                val daily = json.decodeFromString(ArchiveResponse.serializer(), body).daily
                    ?: return@withContext null

                val tempSums = DoubleArray(12)
                val tempCounts = IntArray(12)
                val precipSums = DoubleArray(12)
                var annualMin = Double.MAX_VALUE
                daily.time.forEachIndexed { i, date ->
                    val month = date.substring(5, 7).toIntOrNull() ?: return@forEachIndexed
                    daily.mean.getOrNull(i)?.let { tempSums[month - 1] += it; tempCounts[month - 1]++ }
                    daily.precip.getOrNull(i)?.let { precipSums[month - 1] += it }
                    daily.min.getOrNull(i)?.let { if (it < annualMin) annualMin = it }
                }
                if (tempCounts.any { it == 0 } || annualMin == Double.MAX_VALUE) {
                    return@withContext null // incomplete year
                }
                MonthlyNormals(
                    tempC = List(12) { tempSums[it] / tempCounts[it] },
                    precipMm = List(12) { precipSums[it] },
                    annualMinTempC = annualMin,
                )
            }.getOrNull()
        }

    @Serializable
    private data class ForecastResponse(@SerialName("daily") val daily: ForecastDaily? = null)

    @Serializable
    private data class ForecastDaily(
        @SerialName("temperature_2m_max") val tMax: List<Double?> = emptyList(),
        @SerialName("temperature_2m_min") val tMin: List<Double?> = emptyList(),
        @SerialName("precipitation_sum") val precip: List<Double?> = emptyList(),
        @SerialName("wind_speed_10m_max") val wind: List<Double?> = emptyList(),
    )

    /** Short-range daily forecast (today onward), or null when unreachable. */
    suspend fun forecast(latitude: Double, longitude: Double, days: Int = 7): List<WeatherDay>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = URL(
                    "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=$latitude&longitude=$longitude" +
                        "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max" +
                        "&forecast_days=$days&timezone=auto",
                )
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000; readTimeout = 8000; requestMethod = "GET"
                }
                val body = conn.use { c ->
                    if (c.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                    c.inputStream.bufferedReader().use { it.readText() }
                }
                val d = json.decodeFromString(ForecastResponse.serializer(), body).daily
                    ?: return@withContext null
                val n = d.tMax.size
                if (n == 0) return@withContext null
                (0 until n).mapNotNull { i ->
                    val max = d.tMax.getOrNull(i) ?: return@mapNotNull null
                    val min = d.tMin.getOrNull(i) ?: return@mapNotNull null
                    WeatherDay(
                        dayOffset = i,
                        tempMaxC = max,
                        tempMinC = min,
                        precipMm = d.precip.getOrNull(i) ?: 0.0,
                        windMaxKmh = d.wind.getOrNull(i) ?: 0.0,
                    )
                }
            }.getOrNull()
        }

    private inline fun <T> HttpURLConnection.use(block: (HttpURLConnection) -> T): T =
        try { block(this) } finally { disconnect() }
}
