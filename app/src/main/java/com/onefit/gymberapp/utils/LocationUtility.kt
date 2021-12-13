package com.onefit.gymberapp.utils

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface LocationUtility {

    fun checkLocationEnabled(): Boolean

    fun enableLocation(
        activity: AppCompatActivity,
        locationSettingRequestCode: Int,
        locationEnableListener: (Boolean) -> Unit
    )

    fun checkLocationPermissionsGranted(): Boolean

    fun shouldShowRequestPermissionRationale(
        activity: AppCompatActivity,
        requiredPermissions: Array<String>
    ): Boolean
}

class LocationUtilityImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationUtility {

    override fun checkLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    override fun checkLocationPermissionsGranted(): Boolean {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val permissionStatus = ActivityCompat.checkSelfPermission(context, locationPermission)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    override fun shouldShowRequestPermissionRationale(activity: AppCompatActivity, requiredPermissions: Array<String>): Boolean {
        var showRationale = false
        for (permission in requiredPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true
                break
            }
        }
        return showRationale
    }

    override fun enableLocation(
        activity: AppCompatActivity,
        locationSettingRequestCode: Int,
        locationEnableListener: (Boolean) -> Unit
    ) {

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(
                LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            )

        LocationServices.getSettingsClient(activity)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener { response ->
                val states = response.locationSettingsStates
                locationEnableListener(states?.isLocationPresent == true)
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(activity, locationSettingRequestCode)
                    } catch (sendIntentException: IntentSender.SendIntentException) {
                        locationEnableListener(false)
                    }
                } else {
                    locationEnableListener(false)
                }
            }
    }
}