package io.github.max_schall.appiary.domain.season

/**
 * Picks a [BloomCalendar] for a location: curated calendars for Europe and North
 * America (where the literature is strongest), otherwise a generic calendar keyed
 * by the Köppen climate group. Globally applicable with regional refinement.
 */
object RegionResolver {

    private fun inEurope(lat: Double, lon: Double) = lat in 35.0..71.0 && lon in -11.0..40.0
    private fun inNorthAmerica(lat: Double, lon: Double) = lat in 25.0..60.0 && lon in -130.0..-60.0

    fun resolve(latitude: Double?, longitude: Double?, group: KoppenGroup?): BloomCalendar {
        if (latitude != null && longitude != null) {
            if (inEurope(latitude, longitude)) return BloomCalendars.EUROPE_TEMPERATE
            if (inNorthAmerica(latitude, longitude)) return BloomCalendars.NORTH_AMERICA_TEMPERATE
        }
        return BloomCalendars.generic(group ?: KoppenGroup.TEMPERATE)
    }
}
