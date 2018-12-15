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
import android.os.Bundle
import android.os.ResultReceiver
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.widget.Toast
import com.example.mitchellpatton.mapalarm.data.Alarm
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.*
import kotlin.coroutines.experimental.ContinuationInterceptor

class GeofenceTransitionsIntentService : IntentService("name"){
    private lateinit var triggeredList: List<Geofence>

    companion object{
        val KEY_PLAY_AUDIO = "KEY_PLAY_AUDIO"
        val KEY_ALARM = "KEY_ALARM"
    }

    override fun onHandleIntent(intent: Intent?) {
//        val bundle = Bundle()
//        val receiver: ResultReceiver = intent!!.getParcelableExtra("receiver")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = getString(R.string.error)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {

            triggeredList = geofencingEvent.triggeringGeofences

            val location = geofencingEvent.triggeringLocation
            val gc = Geocoder(this, Locale.getDefault())
            val addrs: List<Address>? =
                    gc.getFromLocation(location.latitude, location.longitude, 1)

            val myAddress = addrs!![0].getAddressLine(0)
//
//            bundle.putString(KEY_PLAY_AUDIO, myAddress)
//            bundle.putString(KEY_ALARM, triggeredList[0].requestId.toString())
//
//            receiver.send(SUCCESS, bundle)

            val myIntent = Intent().setClass(this@GeofenceTransitionsIntentService,
                    MainActivity::class.java)
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            myIntent.putExtra(KEY_PLAY_AUDIO, myAddress)
            myIntent.putExtra(KEY_ALARM, triggeredList[0].requestId.toString())
            startActivity(myIntent)
            stopSelf()
        } else {
            // Log the error.
//            receiver.send(FAILURE, bundle)
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG ).show()
        }
    }

}
