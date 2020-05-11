package com.myhome.realload.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.myhome.realload.model.Image

@Dao
interface ImageDao :BaseDao<Image>{
    @Query("SELECT * FROM image WHERE pid = :pid")
    fun getImagesByPid(pid:Long):List<Image>
    @Query("SELECT * FROM image WHERE pid = :pid")
    fun getLiveImagesByPid(pid:Long):LiveData<List<Image>>
    @Query("DELETE FROM image WHERE pid =:pid")
    fun deleteImagesByPid(pid:Long)
}