package com.geedkajannada.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Goobta qiyaasta ah (coarse) oo qalabka ku jirta. Play Services lama isticmaalo, internet lama isticmaalo;
 * xogta waa maxalli, server looma dirayo.
 */
class LocationProvider(private val context: Context) {
    private val manager get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /** Waxay soo celisaa (lat, lng) ama null haddii goob la'aan ama permission la'aan. */
    @SuppressLint("MissingPermission")
    suspend fun current(): Pair<Double, Double>? {
        val m = manager
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .filter { runCatching { m.isProviderEnabled(it) }.getOrDefault(false) }
        val last = providers.mapNotNull { runCatching { m.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        if (last != null) return last.latitude to last.longitude
        val provider = providers.firstOrNull { it == LocationManager.NETWORK_PROVIDER } ?: return null
        return withTimeoutOrNull(15_000) {
            suspendCancellableCoroutine { cont ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(l: Location) {
                        if (cont.isActive) cont.resume(l.latitude to l.longitude)
                        m.removeUpdates(this)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(p: String?, s: Int, e: Bundle?) = Unit
                    override fun onProviderEnabled(p: String) = Unit
                    override fun onProviderDisabled(p: String) = Unit
                }
                @Suppress("DEPRECATION")
                m.requestSingleUpdate(provider, listener, null)
                cont.invokeOnCancellation { m.removeUpdates(listener) }
            }
        }
    }
}
