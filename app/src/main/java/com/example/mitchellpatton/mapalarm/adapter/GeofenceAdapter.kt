package com.example.mitchellpatton.mapalarm.adapter

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.example.mitchellpatton.mapalarm.GeofenceTransitionsIntentService
import com.example.mitchellpatton.mapalarm.data.Alarm
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceAdapter(val context: Context, val alarmList: List<Alarm>) {

    var geofenceList = mutableListOf<Geofence>()
    var geofencingClient: GeofencingClient
    val GEOFENCE_RADIUS_IN_METERS = 500F


    init{
        initGeofences(alarmList)
        this.geofencingClient = LocationServices.getGeofencingClient(context)
    }

    private fun initGeofences(alarmList: List<Alarm>){
        for (alarm in alarmList){
            geofenceList.add(Geofence.Builder()

                    .setRequestId(alarm.markerId)

                    .setCircularRegion(
                            alarm.alarmLat,
                            alarm.alarmLong,
                            GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)

                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

                    .build())
        }
        Thread{
            startGeofenceActivity()
        }.start()
    }
    private fun startGeofenceActivity(){
        val intent = Intent(context, GeofenceTransitionsIntentService::class.java)

        val geofencePendingIntent= PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if(geofenceList.size != 0) {
                geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {
                    addOnSuccessListener {
                        Toast.makeText(context, "Successful add", Toast.LENGTH_LONG).show()
                    }
                    addOnFailureListener {
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun addGeofence(alarm: Alarm){
        geofenceList.add(Geofence.Builder()

                .setRequestId(alarm.markerId)

                .setCircularRegion(
                        alarm.alarmLat,
                        alarm.alarmLong,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)

                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

                .build())
        Thread{
            startGeofenceActivity()
        } .start()
    }

    fun removeGeofence(requestId: String){
        geofencingClient.removeGeofences(mutableListOf(requestId))?.run {
            addOnSuccessListener {
                Toast.makeText(context,"Successfully removed geofence", Toast.LENGTH_LONG).show()
            }
            addOnFailureListener {
                Toast.makeText(context,"Sadboi hours", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun removeAllGeofence(geofenceList: MutableList<String>){
        geofencingClient.removeGeofences(geofenceList)
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }
}