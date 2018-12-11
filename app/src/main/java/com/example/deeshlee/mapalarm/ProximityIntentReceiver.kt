package com.example.deeshlee.mapalarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.widget.Toast


class ProximityIntentReceiver: BroadcastReceiver() {



    override fun onReceive(context: Context?, intent: Intent?) {
        val key = LocationManager.KEY_PROXIMITY_ENTERING;

        val entering = intent!!.getBooleanExtra(key, false);

        if (entering) {
            Toast.makeText(context as AlarmActivity, "Entering", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context as AlarmActivity, "Exiting", Toast.LENGTH_LONG)
        }


        context.sendNotification()

    }

}