package com.polware.touristplacescolombia

import android.app.Application
import com.polware.touristplacescolombia.roomdb.TouristPlaceDatabase

class TouristPlaceApplication: Application() {

    val database by lazy {
        TouristPlaceDatabase.getInstance(this)
    }
}