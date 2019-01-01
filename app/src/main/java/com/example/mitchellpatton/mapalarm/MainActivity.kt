package com.example.mitchellpatton.mapalarm

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.example.mitchellpatton.mapalarm.GeofenceTransitionsIntentService.Companion.KEY_ALARM
import com.example.mitchellpatton.mapalarm.GeofenceTransitionsIntentService.Companion.KEY_PLAY_AUDIO
import com.example.mitchellpatton.mapalarm.adapter.GeofenceAdapter
import com.example.mitchellpatton.mapalarm.adapter.AlarmAdapter
import com.example.mitchellpatton.mapalarm.data.Alarm
import com.example.mitchellpatton.mapalarm.data.AppDatabase
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place

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
        MyLocationProvider.OnNewLocationAvailable, AlarmDialog.AlarmHandler{

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

    private var firstTime = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        btnSearch.setOnClickListener{
            loadPlaceAutoComplete()
        }

        btnConfirm.isEnabled = false

        Thread {
            alarmList = AppDatabase.getInstance(this@MainActivity).alarmDao().findAllAlarms()

            geofenceAdapter = GeofenceAdapter(this@MainActivity, alarmList)

            alarmAdapter = AlarmAdapter(this@MainActivity, alarmList)
        }.start()

        btnConfirm.setOnClickListener{
            handleAlarmCreate()
        }

        btnList.setOnClickListener{
            startListActivity()
        }

    }

    private fun startListActivity() {
        val intent = Intent()
        intent.setClass(MainActivity@ this, ListActivity::class.java)
        startActivityForResult(intent, LIST_ACTIVITY_REQUEST)
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
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                placeSearchedPin(data)
            }
        }
    }

    private fun placeSearchedPin(data: Intent?) {
        btnConfirm.isEnabled = true
        etNote.setText("")
        etNote.visibility = View.VISIBLE

        val place = PlaceAutocomplete.getPlace(this, data)
        var addressText = place.name.toString()
        addressText += "\n" + place.address.toString()


        val marker = placePlaceMarker(place)
        updateClickedPin(marker)

        mMap.animateCamera(CameraUpdateFactory.newLatLng(clickedPin.position))
    }

    private fun placePlaceMarker(place: Place): Marker {
        val markerOpt = MarkerOptions()
                .position(place.latLng)
                .title(getString(R.string.unconfirmed))
                .draggable(true)
                .icon(bluePin)
        val marker = mMap.addMarker(markerOpt)
        marker.isDraggable

        return marker
    }

    override fun alarmStopped(alarm: Alarm) {
        alarmAdapter.deleteById(alarm.markerId)
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
            while(!(::alarmList.isInitialized)){
                //intentionally empty - waits for alarmList
            }
            runOnUiThread {
                initMarkers(alarmList)
            }
        }.start()

        // Add a marker in Hungary and move the camera
        setInitialView()

        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        //"it" represents lat long where we clicked
        mMap.setOnMapClickListener{
            mapClick(it)
        }

        mMap.setOnMarkerClickListener {
            markerClick(it)
            true
        }
    }

    private fun mapClick(it: LatLng) {
        btnConfirm.isEnabled = true
        etNote.setText("")
        etNote.visibility = View.VISIBLE

        val marker = placeLatLngMarker(it)

        updateClickedPin(marker)

        mMap.animateCamera(CameraUpdateFactory.newLatLng(it))
    }


    private fun updateClickedPin(marker: Marker) {
        checkRemoveClickedPin()
        clickedPin = marker
        checkDisableBtnConfirm()
    }

    private fun checkDisableBtnConfirm() {
        if (clickedPin.title == getString(R.string.confirmed)) {
            btnConfirm.isEnabled = false
        }
    }

    private fun checkRemoveClickedPin() {
        if (clickedPin.title == getString(R.string.unconfirmed)) {
            clickedPin.remove()
        }
    }

    private fun placeLatLngMarker(it: LatLng): Marker {
        val markerOpt = MarkerOptions()
                .position(it)
                .title(getString(R.string.unconfirmed))
                .icon(bluePin)
        val marker = mMap.addMarker(markerOpt)
        marker.isDraggable

        return marker
    }

    private fun markerClick(it: Marker) {
        etNote.visibility = View.VISIBLE
        btnConfirm.isEnabled = true

        if (clickedPin.title == getString(R.string.unconfirmed) && clickedPin != it) {
            clickedPin.remove()
        }

        clickedPin = it

        etNote.setText("")

        checkAlreadyConfirmed(it)

        mMap.animateCamera(CameraUpdateFactory.newLatLng(it.position))
    }

    private fun checkAlreadyConfirmed(it: Marker) {
        if (it.title == getString(R.string.confirmed)) {
            btnConfirm.isEnabled = false
            Toast.makeText(this@MainActivity, getString(R.string.duplicate_alarm), Toast.LENGTH_LONG)
        }
    }

    override fun onResume() {
            if (intent.hasExtra(KEY_PLAY_AUDIO) && intent.hasExtra(KEY_ALARM) && firstTime) {
                firstTime = false
                val alarmToPass = Alarm(1, 0.0, 0.0, intent.getStringExtra(KEY_PLAY_AUDIO), "",
                        intent.getStringExtra(KEY_ALARM))
                showAlarmDialog(alarmToPass)
            }

        super.onResume()

    }

    fun setInitialView(){
        val hungary = LatLng(47.4979, 19.0402)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hungary))
        clickedPin = mMap.addMarker(MarkerOptions().position(hungary)
                .icon(bluePin)
                .title(getString(R.string.unconfirmed)))
        clickedPin.isVisible = false
        btnConfirm.isEnabled = false
        etNote.visibility = View.INVISIBLE
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hungary))
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
            Toast.makeText(this,getString(R.string.select_marker), Toast.LENGTH_LONG).show()
        }
        else {
            val alarmLat = clickedPin.position.latitude
            val alarmLong = clickedPin.position.longitude
            var alarmAddress = ""
            val gc = Geocoder(this, Locale.getDefault())
            val addrs: List<Address>? =
                    gc.getFromLocation(alarmLat, alarmLong, 1)

            if (addrs!!.isEmpty()) {
                alarmAddress = emptyAddress(errorMessage, alarmAddress)

            } else {
                alarmAddress = addrs[0].getAddressLine(0)
            }

            val alarmNote = etNote.text.toString()
            val newAlarm = Alarm(null, alarmLat, alarmLong, alarmAddress, alarmNote, clickedPin.id)

            alarmCreated(
                    newAlarm
            )

            adjustClickedPin()

            geofenceAdapter.addGeofence(newAlarm)

            btnConfirm.isEnabled= false
        }
    }

    private fun adjustClickedPin() {
        clickedPin.tag = clickedPin.id
        markerList.add(clickedPin)
        clickedPin.setIcon(redPin)
        clickedPin.title = getString(R.string.confirmed)
    }

    private fun emptyAddress(errorMessage: String, alarmAddress: String): String {
        var errorMessage1 = errorMessage
        var alarmAddress1 = alarmAddress
        errorMessage1 = getString(R.string.no_address)
        Toast.makeText(this, errorMessage1, Toast.LENGTH_LONG).show()
        alarmAddress1 = getString(R.string.null_message)
        return alarmAddress1
    }

    fun delete_alarm(markerId: String){
        alarmAdapter.deleteById(markerId)
    }


    fun delete_marker(markerId: String){
        var index = 0
        while(markerId != markerList[index].tag) {
            index++
        }
        markerList[index].remove()
    }


    fun showAlarmDialog(alarm: Alarm){
        val alarmDialog = AlarmDialog()

        val bundle = Bundle()
        bundle.putSerializable(getString(R.string.alarm), alarm)
        alarmDialog.arguments = bundle

        alarmDialog.show(supportFragmentManager,
                "SHOWALARM")
    }
}
