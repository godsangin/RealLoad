package com.myhome.realload.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placelog")
class PlaceLog {
    @PrimaryKey(autoGenerate = true) var id:Long = 0
    var latitude:Double = 0.0
    var longitude:Double = 0.0
    var date:String = ""
}