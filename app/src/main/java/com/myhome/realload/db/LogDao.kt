package com.myhome.realload.db

import androidx.room.Dao
import androidx.room.Query
import com.myhome.realload.model.PlaceLog

@Dao
interface LogDao :BaseDao<PlaceLog>{
    @Query("SELECT * FROM placelog WHERE date BETWEEN :startDate AND :nextDate ORDER BY date")
    fun selectByDateSyn(startDate:String, nextDate:String):List<PlaceLog>?

}