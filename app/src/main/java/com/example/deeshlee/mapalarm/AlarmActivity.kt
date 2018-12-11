package com.example.deeshlee.mapalarm

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_alarm.*

class AlarmActivity : AppCompatActivity() {
    val PROX_ALERT_INTENT = "com.example.deeshlee.mapalarm.AlarmActivity"

    private lateinit var locationManager: LocationManager

    private val MINIMUM_DISTANCECHANGE_FOR_UPDATE: Float = 1F

    private val  MINIMUM_TIME_BETWEEN_UPDATE: Long = 1000

    private lateinit var notificationManager: NotificationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        getSystemService(Context.LOCATION_SERVICE)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Here", Toast.LENGTH_LONG).show()
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MINIMUM_TIME_BETWEEN_UPDATE,
                    MINIMUM_DISTANCECHANGE_FOR_UPDATE,
                    locationListener
            )
        }

        createNotificationChannel(
                "com.example.deeshlee.mapalarm.ProximityAlert",
                "NotifyDemo News",
                "Example News Channel")

        btnNotify.setOnClickListener {
            sendNotification()
        }

        addProximityAlert()
    }

    private fun saveProximityAlertPoint(){
    }

    private fun addProximityAlert(){
        val lat = intent.getDoubleExtra("lat", 47.0)
        val lng = intent.getDoubleExtra("lng", 19.0)
        val intent = Intent(PROX_ALERT_INTENT)
        val pi = PendingIntent.getBroadcast(this, 0, intent, 0)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        locationManager.addProximityAlert(lat, lng, 1000F, -1, pi)
        }

        val filter = IntentFilter(PROX_ALERT_INTENT)
        registerReceiver(ProximityIntentReceiver(), filter);
    }


    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }


    fun sendNotification() {

        val channelID = "com.example.deeshlee.mapalarm"

        val notification = Notification.Builder(this@AlarmActivity,
                channelID)
                .setContentTitle("Example Notification")
                .setContentText("This is an  example notification.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setChannelId(channelID)
                .build()

        notificationManager.notify(100,notification)
    }

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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(ProximityIntentReceiver())
    }
}
