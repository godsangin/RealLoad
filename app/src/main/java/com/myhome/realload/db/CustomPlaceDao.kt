package com.myhome.realload.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.myhome.realload.model.CustomPlace

@Dao
interface CustomPlaceDao:BaseDao<CustomPlace>{
    @Query("SELECT * FROM myPlace WHERE id = :id")
    fun selectById(id:Long):CustomPlace

    @Query("SELECT * FROM myPlace")
    fun selectAll():LiveData<List<CustomPlace>>
    @Query("SELECT * FROM myPlace")
    fun selectAllSync():List<CustomPlace>

    @Query("SELECT * FROM myPlace ORDER BY ABS(longitude - :longitude + latitude - :latitude)")
    fun selectByDistance(latitude:Double, longitude:Double):CustomPlace

    @Query("SELECT * FROM myPlace WHERE favorite ORDER BY ABS(longitude - :longitude + latitude - :latitude)")
    fun selectFavoriteByDistance(latitude:Double, longitude:Double):CustomPlace

    @Query("SELECT * FROM myPlace WHERE favorite")
    fun selectFavoritePlace():List<CustomPlace>

    @Query("UPDATE myPlace SET favorite = :favorite WHERE id = :id")
    fun updateFavorite(id: Long, favorite:Boolean)


}