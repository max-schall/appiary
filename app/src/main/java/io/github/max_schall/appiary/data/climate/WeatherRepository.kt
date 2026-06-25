package io.github.max_schall.appiary.data.climate

import io.github.max_schall.appiary.domain.weather.WeatherDay
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Short-range forecast for weather-timed recommendations. Online-only by design
 * (no offline fallback): when unreachable it returns an empty list and the
 * weather rules simply produce nothing. A brief in-memory cache avoids hammering
 * the API across back-to-back engine runs. Only coordinates leave the device.
 */
class WeatherRepository(
    private val client: OpenMeteoClient = OpenMeteoClient(),
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private data class Entry(val days: List<WeatherDay>, val at: Long)

    private val cache = mutableMapOf<String, Entry>()
    private val ttlMs = TimeUnit.HOURS.toMillis(3)

    suspend fun forecast(latitude: Double, longitude: Double): List<WeatherDay> {
        val key = String.format(Locale.US, "%.2f,%.2f", latitude, longitude)
        cache[key]?.let { if (clock() - it.at < ttlMs) return it.days }
        val fresh = client.forecast(latitude, longitude)
            ?: return cache[key]?.days ?: emptyList()
        cache[key] = Entry(fresh, clock())
        return fresh
    }
}
