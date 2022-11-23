package com.polware.touristplacescolombia

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.polware.touristplacescolombia.databinding.ActivityMapBinding
import com.polware.touristplacescolombia.roomdb.TouristPlaceEntity

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var bindingMap: ActivityMapBinding
    private var touristPlace: TouristPlaceEntity? = null
    lateinit var placeOnMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingMap = ActivityMapBinding.inflate(layoutInflater)
        setContentView(bindingMap.root)

        if (intent.hasExtra(MainActivity.PLACE_DETAILS)) {
            touristPlace = intent.getParcelableExtra(MainActivity.PLACE_DETAILS) as TouristPlaceEntity?
        }

        if (touristPlace != null){
            setSupportActionBar(bindingMap.toolbarGMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = touristPlace!!.title

            bindingMap.toolbarGMap.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        val supporMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        supporMapFragment.getMapAsync(this)

    }

    // Display a marker on Google Map
    override fun onMapReady(googleMap: GoogleMap) {
        placeOnMap = googleMap
        placeOnMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            placeOnMap.isMyLocationEnabled = true
            placeOnMap.uiSettings.isZoomControlsEnabled = true
            placeOnMap.uiSettings.isCompassEnabled = true
        }

        val placePosition = LatLng(touristPlace!!.latitude, touristPlace!!.longitude)
        placeOnMap.addMarker(MarkerOptions().position(placePosition).title(touristPlace!!.location))
        val placeZoom = CameraUpdateFactory.newLatLngZoom(placePosition, 12f)
        placeOnMap.animateCamera(placeZoom)

    }

}