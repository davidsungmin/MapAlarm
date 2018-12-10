package com.example.mitchellpatton.mapalarm

import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*


class MyLocationProvider(context: Context,
                         private val onNewLocationAvailable: OnNewLocationAvailable)  {

    //represents location monitoring
    private val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)


    private var locationCallback: LocationCallback = object : LocationCallback() {
        //locationResult.lastLocation is the current location
        //contains .lat .lng info
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            onNewLocationAvailable.onNewLocation(locationResult.lastLocation)
        }
    }


    interface OnNewLocationAvailable {
        fun onNewLocation(location: Location)
    }

    @Throws(SecurityException::class)
    fun startLocationMonitoring() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.myLooper())

    }

    @Throws(SecurityException::class)
    //just removes location updates
    fun stopLocationMonitoring() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}