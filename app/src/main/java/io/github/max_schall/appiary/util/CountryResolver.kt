package io.github.max_schall.appiary.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Resolves an ISO-3166 alpha-2 country code from coordinates via reverse
 * geocoding (online). Falls back to a coarse Germany bounding box so the
 * Bestandsbuch features still activate offline for clearly-German locations.
 */
object CountryResolver {

    suspend fun resolve(context: Context, latitude: Double, longitude: Double): String? {
        geocodeCountry(context, latitude, longitude)?.let { return it.uppercase(Locale.ROOT) }
        // Offline / geocoder-unavailable fallback (rough Germany box).
        if (latitude in 47.27..55.06 && longitude in 5.87..15.04) return "DE"
        return null
    }

    private suspend fun geocodeCountry(context: Context, lat: Double, lon: Double): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                runCatching {
                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            cont.resume(addresses.firstOrNull()?.countryCode)
                        }
                        override fun onError(errorMessage: String?) { cont.resume(null) }
                    })
                }.onFailure { cont.resume(null) }
            }
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.countryCode
                }.getOrNull()
            }
        }
    }
}
