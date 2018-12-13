package com.example.mitchellpatton.mapalarm

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.media.RingtoneManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.*
import kotlin.coroutines.experimental.ContinuationInterceptor

class GeofenceTransitionsIntentService : IntentService("name") {

    private lateinit var triggeredList: List<Geofence>

    private lateinit var notificationManager: NotificationManager

    companion object{
        val KEY_PLAY_AUDIO = "KEY_PLAY_AUDIO"
        val KEY_ALARM = "KEY_ALARM"
    }

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = getString(R.string.error)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            triggeredList = geofencingEvent.triggeringGeofences

            val location = geofencingEvent.triggeringLocation
            val gc = Geocoder(this, Locale.getDefault())
            val addrs: List<Address>? =
                    gc.getFromLocation(location.latitude, location.longitude, 1)

            val myAddress = addrs!![0].getAddressLine(0)


            val myIntent = Intent().setClass(this@GeofenceTransitionsIntentService,
                    ListActivity::class.java)
            myIntent.putExtra(KEY_PLAY_AUDIO, myAddress)
            //myIntent.putExtra(KEY_ALARM, triggeredList[0].requestId.toString())
            startActivity(myIntent)


        } else {
            // Log the error.
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG ).show()
        }

        notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotification() {

        val channelID = getString(R.string.mitchellpatton_mapalarm)

        val notification = Notification.Builder(this@GeofenceTransitionsIntentService,
                channelID)
                .setContentTitle(getString(R.string.example_notification))
                .setContentText(getString(R.string.example_context))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setChannelId(channelID)
                .build()

        notificationManager.notify(100, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String, name: String,
                                          description: String) {

        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }

}
