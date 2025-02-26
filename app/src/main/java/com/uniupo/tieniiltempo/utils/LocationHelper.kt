// app/src/main/java/com/uniupo/tieniiltempo/utils/LocationHelper.kt
package com.uniupo.tieniiltempo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCoroutine { continuation ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                continuation.resume(location)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMs: Long = 10000): Flow<Location> = callbackFlow {
        val locationRequest = LocationRequest.Builder(intervalMs)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    fun isLocationWithinRange(
        currentLat: Double,
        currentLng: Double,
        targetLat: Double,
        targetLng: Double,
        radiusMeters: Double = 100.0
    ): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLat, currentLng,
            targetLat, targetLng,
            results
        )
        return results[0] <= radiusMeters
    }
}