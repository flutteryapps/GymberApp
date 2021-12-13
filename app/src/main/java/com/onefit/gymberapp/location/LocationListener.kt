package com.onefit.gymberapp.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class LocationListener(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val callback: (Location?) -> Unit
): LocationCallback(), DefaultLifecycleObserver {

    private var enabled = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onStart(owner: LifecycleOwner) {
        if (enabled) {
            fetchLocation()
        }
    }

    fun getLocation() {
        enabled = true
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            fetchLocation()
        }
    }

    private fun fetchLocation() {

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        callback(location)
                    } else {

                        lifecycle.coroutineScope.launch {
                            fusedLocationClient.requestLocationUpdates(createLocationRequest(), this@LocationListener, Looper.getMainLooper())
                            delay(3000)
                            callback(null)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    callback(null)
                    Log.d(TAG, exception.toString())
                }
        } else {
            callback(null)
        }
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setFastestInterval(2000)
            .setNumUpdates(1)
    }

    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)
        callback(locationResult.lastLocation)

        Log.d(TAG, "${locationResult.lastLocation.latitude} : ${locationResult.lastLocation.longitude}")
    }

    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
        super.onLocationAvailability(locationAvailability)

        if (locationAvailability.isLocationAvailable) {
            Log.d(TAG, "Location is available!")
        }
    }

    companion object {
        private const val TAG = "LocationListener"
    }
}
