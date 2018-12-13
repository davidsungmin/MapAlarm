package com.example.mitchellpatton.mapalarm

import android.Manifest
import android.app.PendingIntent
import android.content.Intent

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.example.mitchellpatton.mapalarm.adapter.GeofenceAdapter
import com.example.mitchellpatton.mapalarm.adapter.AlarmAdapter
import com.example.mitchellpatton.mapalarm.data.Alarm
import com.example.mitchellpatton.mapalarm.data.AppDatabase
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlaceAutocomplete

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.livinglifetechway.quickpermissions.annotations.WithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
        MyLocationProvider.OnNewLocationAvailable{

    private lateinit var mMap: GoogleMap
    private lateinit var bluePin: BitmapDescriptor
    private lateinit var redPin: BitmapDescriptor

    private lateinit var clickedPin: Marker
    private lateinit var alarmAdapter: AlarmAdapter

    private val PLACE_PICKER_REQUEST = 1001
    private val LIST_ACTIVITY_REQUEST = 1002

    private lateinit var markerList: MutableList<Marker>

    private lateinit var alarmList: List<Alarm>

    private lateinit var geofenceAdapter: GeofenceAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnSearch.setOnClickListener{
            loadPlaceAutoComplete()
        }

        btnConfirm.isEnabled = false

        btnConfirm.setOnClickListener{
            handleAlarmCreate()
        }

        btnList.setOnClickListener{
            val intent = Intent()
            intent.setClass(MainActivity@this, ListActivity::class.java)
            startActivityForResult(intent, LIST_ACTIVITY_REQUEST)
        }


    }

    fun initMarkers(alarmList: List<Alarm>){
        markerList = mutableListOf()
        for (alarm in alarmList){
            val markerOpt = MarkerOptions()
                    .position(LatLng(alarm.alarmLat,alarm.alarmLong))
                    .draggable(true)
                    .icon(redPin)

            val newMarker = mMap.addMarker(markerOpt)
            newMarker.tag = alarm.markerId
            markerList.add(newMarker)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LIST_ACTIVITY_REQUEST){
            if (resultCode == RESULT_OK){
                val markersToDelete = data!!.getStringArrayListExtra("markersToDelete")
                for (markerId in markersToDelete){
                    delete_marker(markerId)
                }
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()


                val markerOpt = MarkerOptions()
                        .position(place.latLng)
                        .title("Unconfirmed")
                        .draggable(true)
                        .icon(bluePin)

                val marker = mMap.addMarker(markerOpt)

                clickedPin = marker

                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.latLng))

            }
        }
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

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        bluePin = BitmapDescriptorFactory.fromResource(R.drawable.bluepin)
        redPin = BitmapDescriptorFactory.fromResource(R.drawable.redpin)


        Thread{
            alarmList = AppDatabase.getInstance(this@MainActivity).alarmDao().findAllAlarms()

            geofenceAdapter = GeofenceAdapter(this@MainActivity, alarmList)

            alarmAdapter = AlarmAdapter(this@MainActivity, alarmList)

            runOnUiThread{
                initMarkers(alarmList)
            }
        }.start()

        // Add a marker in Hungary and move the camera
        val hungary = LatLng(47.4979, 19.0402)
        clickedPin = mMap.addMarker(MarkerOptions().position(hungary).icon(bluePin).title("Unconfirmed"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hungary))

        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true


        //"it" represents lat long where we clicked
        mMap.setOnMapClickListener{

            btnConfirm.isEnabled = true
            etNote.setText("")
            val markerOpt = MarkerOptions()
                    .position(it)
                    .title("Unconfirmed")
                    .icon(bluePin)
            val marker = mMap.addMarker(markerOpt)

            if (clickedPin.title == "Unconfirmed"){
                clickedPin.remove()
            }

            clickedPin = marker

            if (clickedPin.title == "Confirmed"){
                btnConfirm.isEnabled = false
            }

            marker.isDraggable = true

            //moves the camera slowly
            //more camera stuff in the slides/ dem
            mMap.animateCamera(CameraUpdateFactory.newLatLng(it))
            //mMap.clear()

        }


        //must be declared globally
        //not indivdually
        //it represents the marker that was clicked
        mMap.setOnMarkerClickListener {
            //it.title
            //it.remove()
            //it.position
            //it.setIcon()
            btnConfirm.isEnabled=true

            if (clickedPin.title == "Unconfirmed" && clickedPin!= it){
                clickedPin.remove()
            }

            clickedPin = it

            // etNote.text = it.id
            etNote.setText("")

            etNote.visibility = View.VISIBLE
            if(it.title=="Confirmed"){
                btnConfirm.isEnabled = false
                Toast.makeText(this@MainActivity, "An alarm already exists at this location", Toast.LENGTH_LONG)
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLng(it.position))

            true }

    }

    private fun loadPlaceAutoComplete() {
        val myIntent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                .build(this@MainActivity)

        try {
            startActivityForResult(myIntent, PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
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

    private fun handleAlarmCreate() {
        var errorMessage = ""

        if (clickedPin == null){
            Toast.makeText(this,"Please select a marker", Toast.LENGTH_LONG).show()
        }
        else {
            val alarmLat = clickedPin.position.latitude
            val alarmLong = clickedPin.position.longitude
            var alarmAddress = ""
            val gc = Geocoder(this, Locale.getDefault())
            val addrs: List<Address>? =
                    gc.getFromLocation(alarmLat, alarmLong, 1)

            if (addrs!!.isEmpty()) {
                errorMessage = "No Address found"
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                alarmAddress = "None found"

            } else {
                alarmAddress = addrs[0].getAddressLine(0)
            }
            val alarmNote = etNote.text.toString()
            val newAlarm = Alarm(null, alarmLat, alarmLong, alarmAddress, alarmNote, clickedPin.id)
            clickedPin.tag = clickedPin.id
            markerList.add(clickedPin)

            alarmCreated(
                    newAlarm
            )
            clickedPin.setIcon(redPin)
            clickedPin.title = "Confirmed"

            geofenceAdapter.addGeofence(newAlarm)


            btnConfirm.isEnabled= false

        }
    }


    fun delete_marker(markerId: String){
        var index = 0
        while(markerId != markerList[index].tag){
            index++
        }
        markerList[index].remove()
        markerList.removeAt(index)
    }

}
