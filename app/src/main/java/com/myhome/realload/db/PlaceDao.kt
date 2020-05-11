package com.myhome.realload.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.myhome.realload.model.Place
import java.util.*

@Dao
interface PlaceDao:BaseDao<Place>{
    @Query("SELECT * FROM place WHERE id = :id")
    fun selectById(id: Long): LiveData<Place>

    @Query("SELECT * FROM place WHERE latitude = :latitude and longitude = :longitude")
    fun selectByLocation(latitude:Double, longitude:Double): Place?

    @Query("SELECT * FROM place WHERE startDate = :startDate")
    fun selectByStartDate(startDate:String): Place?

    @Query("SELECT * FROM place ORDER BY startDate")
    fun selectAllPlace():LiveData<List<Place>>

    @Query("SELECT * FROM place")
    fun selectAll():List<Place>


    @Query("SELECT * FROM place WHERE startDate BETWEEN :startDate AND :nextDate OR endDate BETWEEN :startDate AND :nextDate ORDER BY startDate")
    fun selectByDate(startDate:String, nextDate:String):LiveData<List<Place>>

    @Query("SELECT * FROM place WHERE startDate BETWEEN :startDate AND :nextDate OR endDate BETWEEN :startDate AND :nextDate ORDER BY startDate")
    fun selectByDateSyn(startDate:String, nextDate:String):List<Place>?

    @Query("SELECT * FROM place WHERE startDate = :startDate")
    fun selectByDate(startDate:String):LiveData<List<Place>>

    @Query("UPDATE place SET endDate = :endDate WHERE latitude = :latitude and longitude = :longitude")
    fun updateEndDate(latitude: Double, longitude: Double, endDate:String)

    @Query("DELETE FROM place WHERE startDate = :startDate")
    fun deleteByDate(startDate: String)
}