package com.example.mitchellpatton.mapalarm
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.Status

import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.livinglifetechway.quickpermissions.annotations.WithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


class MainActivity : AppCompatActivity(), OnMapReadyCallback, MyLocationProvider.OnNewLocationAvailable{


    private lateinit var mMap: GoogleMap
    private lateinit var bluePin: BitmapDescriptor
    private lateinit var redPin: BitmapDescriptor
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
        tvAddress.text = "Loc: ${location.latitude}, ${location.longitude}"
    }

    //Location stuff done
    //Map stuff/ interactivity BY user below

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        bluePin = BitmapDescriptorFactory.fromResource(R.drawable.bluepin)
        redPin = BitmapDescriptorFactory.fromResource(R.drawable.redpin)

        // Add a marker in Hungary and move the camera
        val hungary = LatLng(47.4979, 19.0402)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hungary))

        mMap.uiSettings.isZoomControlsEnabled = true


        //"it" represents lat long where we clicked
        mMap.setOnMapClickListener{
            val markerOpt = MarkerOptions()
                    .position(it)
                    .title("Set alarm ${it.latitude}, ${it.longitude}")
                    .icon(bluePin)
            val marker = mMap.addMarker(markerOpt)
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
            it.setIcon(redPin)
            //it.title
            //it.remove()
            //it.position
            //it.setIcon()

            //probably call AlarmActivity
            true
            //false means "now do the default info window stuff"
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


}