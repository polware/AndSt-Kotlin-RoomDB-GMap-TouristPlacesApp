package com.polware.touristplacescolombia

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.polware.touristplacescolombia.databinding.ActivityPlaceDetailsBinding
import com.polware.touristplacescolombia.roomdb.TouristPlaceEntity

class PlaceDetailsActivity : AppCompatActivity() {
    lateinit var bindingPlaceDetails: ActivityPlaceDetailsBinding
    private var touristPlaceEntity: TouristPlaceEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingPlaceDetails = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(bindingPlaceDetails.root)

        touristPlaceEntity = intent.getParcelableExtra(MainActivity.PLACE_DETAILS) as TouristPlaceEntity?
        bindingPlaceDetails.imageViewDetails.setImageURI(Uri.parse(touristPlaceEntity!!.image))
        bindingPlaceDetails.textViewDetailDescription.text = touristPlaceEntity!!.description
        bindingPlaceDetails.textViewLocation.text = touristPlaceEntity!!.location

        setSupportActionBar(bindingPlaceDetails.toolbarPlaceDetails)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = touristPlaceEntity!!.title
        bindingPlaceDetails.toolbarPlaceDetails.setNavigationOnClickListener {
            onBackPressed()
        }

        bindingPlaceDetails.buttonViewOnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra(MainActivity.PLACE_DETAILS, touristPlaceEntity)
            startActivity(intent)
        }

    }

}