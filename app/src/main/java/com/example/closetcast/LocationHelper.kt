package com.example.closetcast

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper(private val context: Context) {
    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(onLocationReceived: (Double, Double) -> Unit) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        Log.d("LocationHelper", "requestCurrentLocation 시작")

        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                Log.d("LocationHelper", "위치 얻음: lat=${location.latitude}, lng=${location.longitude}")
                onLocationReceived(location.latitude, location.longitude)
            } else {
                Log.e("LocationHelper", "위치 null")
            }
        }.addOnFailureListener { e ->
            Log.e("LocationHelper", "위치 요청 실패", e)
        }
    }
}