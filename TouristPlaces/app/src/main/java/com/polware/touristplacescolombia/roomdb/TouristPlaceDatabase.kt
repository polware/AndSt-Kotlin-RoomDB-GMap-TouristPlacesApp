package com.polware.touristplacescolombia.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TouristPlaceEntity::class], version = 1)
abstract class TouristPlaceDatabase: RoomDatabase() {

    abstract fun touristPlaceDAO(): TouristPlaceDAO

    companion object {
        @Volatile
        private var INSTANCE: TouristPlaceDatabase? = null

        fun getInstance(context: Context): TouristPlaceDatabase{
            synchronized(this){
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext,
                        TouristPlaceDatabase::class.java, "touristplace_database")
                        .fallbackToDestructiveMigration().build() // Wipes and rebuild instead of migrating
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}