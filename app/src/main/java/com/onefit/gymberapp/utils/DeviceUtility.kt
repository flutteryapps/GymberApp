package com.onefit.gymberapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import javax.inject.Inject

interface DeviceUtility {

    fun hasActiveConnection(): Boolean

    fun getDeviceDensity(): DeviceDensity?
}

class DeviceUtilityImpl @Inject constructor(
    private val context: Context
) : DeviceUtility {

    override fun hasActiveConnection(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    override fun getDeviceDensity(): DeviceDensity? {
        return when (context.resources.displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> DeviceDensity.LDPI
            DisplayMetrics.DENSITY_MEDIUM -> DeviceDensity.MDPI
            DisplayMetrics.DENSITY_HIGH -> DeviceDensity.HDPI
            DisplayMetrics.DENSITY_XHIGH, DisplayMetrics.DENSITY_280 -> DeviceDensity.XHDPI
            DisplayMetrics.DENSITY_XXHIGH, DisplayMetrics.DENSITY_360, DisplayMetrics.DENSITY_400, DisplayMetrics.DENSITY_420 -> DeviceDensity.XXHDPI
            DisplayMetrics.DENSITY_XXXHIGH, DisplayMetrics.DENSITY_560 -> DeviceDensity.XXXHDPI
            else -> null
        }
    }
}