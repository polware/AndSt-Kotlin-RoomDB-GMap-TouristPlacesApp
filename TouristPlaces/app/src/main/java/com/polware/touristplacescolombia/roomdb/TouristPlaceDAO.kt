package com.polware.touristplacescolombia.roomdb

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TouristPlaceDAO {

    @Insert
    suspend fun insert(touristPlaceEntity: TouristPlaceEntity)

    @Update
    suspend fun update(touristPlaceEntity: TouristPlaceEntity)

    @Delete
    suspend fun delete(touristPlaceEntity: TouristPlaceEntity)

    // Flow nos retorna automáticamente los valores modificados
    @Query("SELECT * FROM `touristplace-table`")
    fun getAllTouristPlaces(): Flow<List<TouristPlaceEntity>>

    @Query("SELECT * FROM `touristplace-table` WHERE id=:id")
    fun getTouristPlaceById(id: Int): Flow<TouristPlaceEntity>

}