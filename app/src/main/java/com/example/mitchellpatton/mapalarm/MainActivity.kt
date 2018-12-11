package com.example.mitchellpatton.mapalarm
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.mitchellpatton.mapalarm.adapter.AlarmAdapter
import com.example.mitchellpatton.mapalarm.data.Alarm
import com.example.mitchellpatton.mapalarm.data.AppDatabase
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.Status

import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.livinglifetechway.quickpermissions.annotations.WithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.*
import com.google.android.gms.maps.model.*
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback, MyLocationProvider.OnNewLocationAvailable{

    private lateinit var mMap: GoogleMap
    private lateinit var bluePin: BitmapDescriptor
    private lateinit var redPin: BitmapDescriptor

    private lateinit var clickedPin: Marker
    private lateinit var alarmAdapter: AlarmAdapter

    private val PLACE_PICKER_REQUEST = 1001


   // private val redPin = BitmapDescriptorFactory.fromResource(R.drawable.redpin)

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()


                val markerOpt = MarkerOptions()
                        .position(place.latLng)
                        .draggable(true)
                        .icon(bluePin)
                mMap.addMarker(markerOpt)

                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.latLng))

            }
        }
    }

    //
    //Location sharing stuff
    private lateinit var myLocationProvider: MyLocationProvider

    override fun onStart() {
        super.onStart()
        startLocation()

    }

    @WithPermissions(
            permissions = [android.Manifest.permission.ACCESS_FINE_LOCATION]
    )


    fun startLocation(){
        myLocationProvider = MyLocationProvider(this,
                this)
        myLocationProvider.startLocationMonitoring()
    }

    override fun onStop() {
        super.onStop()
        myLocationProvider.stopLocationMonitoring()
    }

    override fun onNewLocation(location: Location) {
//        tvAddress.text = "Loc: ${location.latitude}, ${location.longitude}"
    }

    //Location stuff done
    //Map stuff/ interactivity BY user below

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        bluePin = BitmapDescriptorFactory.fromResource(R.drawable.bluepin)
        redPin = BitmapDescriptorFactory.fromResource(R.drawable.redpin)

        // Add a marker in Hungary and move the camera
        val hungary = LatLng(47.4979, 19.0402)
        clickedPin = mMap.addMarker(MarkerOptions().position(hungary).icon(bluePin).title("Unconfirmed"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hungary))

        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true


        //"it" represents lat long where we clicked
        mMap.setOnMapClickListener{
            val markerOpt = MarkerOptions()
                    .position(it)
                    .title("Unconfirmed")
                    .icon(bluePin)
            val marker = mMap.addMarker(markerOpt)

            if (clickedPin.title == "Unconfirmed"){
                clickedPin.remove()
            }

            clickedPin = marker

            etNote.visibility = View.INVISIBLE

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

            if (clickedPin.title == "Unconfirmed" && clickedPin!= it){
                clickedPin.remove()
            }

            clickedPin = it

            etNote.visibility = View.VISIBLE
            true
        }

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

    private fun handleAlarmCreate(context: Context) {
        var errorMessage = ""

        if (clickedPin == null){
            Toast.makeText(this,"Please select a marker", Toast.LENGTH_LONG).show()
        } else {
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

            alarmCreated(
                    Alarm(
                            null,
                            alarmLat,
                            alarmLong,
                            alarmAddress,
                            alarmNote
                    )
            )

            clickedPin.setIcon(redPin)
            clickedPin.title = "Confirmed"
        }
    }


}