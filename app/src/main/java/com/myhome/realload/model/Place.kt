package com.myhome.realload.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "place")
open class Place{
    @PrimaryKey(autoGenerate = true) var id:Long = 0
    var latitude:Double = 0.0
    var longitude:Double = 0.0
    var startDate:String = ""
    var endDate:String = ""
    var readOnly:Boolean = false
    override fun toString(): String {
        return "id=" + id.toString() + "\nlatitude=" + latitude.toString() + "\nlongitude=" + longitude.toString() + "\nstartDate=" + startDate + "\nendDate=" + endDate + "\nreadOnly=" + readOnly.toString()
    }
}