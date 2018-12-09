package com.example.deeshlee.mapalarm

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.deeshlee.mapalarm.adapter.AlarmAdapter
import com.example.deeshlee.mapalarm.data.Alarm
import com.example.deeshlee.mapalarm.data.AppDatabase

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.livinglifetechway.k4kotlin.TAG
import com.livinglifetechway.quickpermissions.annotations.WithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
        MyLocationProvider.OnNewLocationAvailable{

    private lateinit var mMap: GoogleMap

    private lateinit var clickedPin: Marker


    private lateinit var alarmAdapter: AlarmAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnConfirm.setOnClickListener{
            handleAlarmCreate(this)
        }

        Thread{
        val alarmList = AppDatabase.getInstance(
                this@MainActivity
        ).alarmDao().findAllAlarms()

        alarmAdapter = AlarmAdapter(this@MainActivity, alarmList)
        }.start()

        btnList.setOnClickListener{
            val intent = Intent()
            intent.setClass(MainActivity@this, ListActivity::class.java)
            startActivity(intent)
        }




//        btnNormal.setOnClickListener {
//            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//
//            mMap.clear()
//        }
//        btnSatellite.setOnClickListener {
//            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
//
//            val cameraPosition = CameraPosition.Builder()
//                    .target(LatLng(47.0, 19.0))
//                    .zoom(17f)
//                    .bearing(90f)
//                    .tilt(30f)
//                    .build()
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
//                    cameraPosition))
//
//
//        }
    }

    private lateinit var myLocationProvider: MyLocationProvider

    override fun onStart() {
        super.onStart()
        startLocation()
    }

    @WithPermissions(
            permissions = [android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun startLocation() {
        myLocationProvider = MyLocationProvider(this,
                this)
        myLocationProvider.startLocationMonitoring()
    }


    override fun onStop() {
        super.onStop()
        myLocationProvider.stopLocationMonitoring()
    }

    override fun onNewLocation(location: Location) {
//        tvData.text =
//                "Loc: ${location.latitude}, ${location.longitude}"
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val marker = LatLng(47.0, 19.0)
        mMap.addMarker(MarkerOptions().position(marker).title("Marker in Hungary"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL


        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true


        mMap.setOnMapClickListener {
            val markerOpt = MarkerOptions().
                    position(it).
                    title("My marker ${it.latitude}, ${it.longitude}")
            val marker = mMap.addMarker(markerOpt)

            etNote.visibility = View.INVISIBLE

            marker.isDraggable = true


            mMap.animateCamera(CameraUpdateFactory.newLatLng(it))
        }


        mMap.setOnMarkerClickListener {
            Toast.makeText(this@MainActivity, it.title,
                    Toast.LENGTH_LONG).show()

            clickedPin = it

            etNote.visibility = View.VISIBLE

            true
        }

    }


    private fun alarmCreated(alarm: Alarm) {
        Thread {
            val alarmId = AppDatabase.getInstance(
                    this@MainActivity).alarmDao().insertAlarm(alarm)

            alarm.alarmId = alarmId

            runOnUiThread {
                alarmAdapter.addAlarm(alarm)
            }
        }.start()
    }

    private fun handleAlarmCreate(context: Context) {
        var errorMessage = ""

        if (clickedPin == null){
            Toast.makeText(this,"Please select a marker", Toast.LENGTH_LONG).show()
        }
        val alarmLat = clickedPin.position.latitude
        val alarmLong = clickedPin.position.longitude
        var alarmAddress = ""
        val gc = Geocoder(this, Locale.getDefault())
        val addrs: List<Address>? =
                gc.getFromLocation(alarmLat, alarmLong, 1)

        if (addrs!!.isEmpty()) {
            errorMessage = "No Address found"
            Toast.makeText(this,errorMessage,Toast.LENGTH_LONG ).show()
            alarmAddress = "None found"

        } else {
            alarmAddress = addrs[0].getAddressLine(0)
        }
        val alarmNote = etNote.text.toString()

        alarmCreated(
                Alarm(
                        null,
                        alarmLat,
                        alarmLong,
                        alarmAddress,
                        alarmNote
                )
        )
    }

}

