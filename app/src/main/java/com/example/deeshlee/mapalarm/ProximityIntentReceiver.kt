package com.example.deeshlee.mapalarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.graphics.Color
import android.location.LocationManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.deeshlee.mapalarm.data.Alarm


class ProximityIntentReceiver: BroadcastReceiver() {



    override fun onReceive(context: Context?, intent: Intent?) {
        val key = LocationManager.KEY_PROXIMITY_ENTERING;

        val entering = intent!!.getBooleanExtra(key, false);

        if (entering) {
            Toast.makeText(context as AlarmActivity, "Entering", Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(context as AlarmActivity, "Exiting", Toast.LENGTH_LONG)
        }


        context.sendNotification()
//        notificationManager =
//                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
//
//        createNotificationChannel(
//                "com.example.deeshlee.mapalarm",
//                "NotifyDemo News",
//                "Example News Channel")

    }
//    private fun createNotificationChannel(id: String, name: String,
//                                          description: String) {
//
//        val importance = NotificationManager.IMPORTANCE_LOW
//        val channel = NotificationChannel(id, name, importance)
//
//        channel.description = description
//        channel.enableLights(true)
//        channel.lightColor = Color.RED
//        channel.enableVibration(true)
//        channel.vibrationPattern =
//                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
//        notificationManager?.createNotificationChannel(channel)
//    }

}