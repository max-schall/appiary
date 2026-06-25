package io.github.max_schall.appiary.domain.weather

/** One day of forecast for a location. [dayOffset] 0 = today, 1 = tomorrow, … */
data class WeatherDay(
    val dayOffset: Int,
    val tempMaxC: Double,
    val tempMinC: Double,
    val precipMm: Double,
    val windMaxKmh: Double,
)
