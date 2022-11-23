package com.polware.touristplacescolombia.roomdb

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "touristplace-table")
data class TouristPlaceEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String? = "",
    @ColumnInfo(name = "image-place")
    val image: String? = "",
    val description: String? = "",
    val date: String? = "",
    val location: String? = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0

): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TouristPlaceEntity> {
        override fun createFromParcel(parcel: Parcel): TouristPlaceEntity {
            return TouristPlaceEntity(parcel)
        }

        override fun newArray(size: Int): Array<TouristPlaceEntity?> {
            return arrayOfNulls(size)
        }
    }
}