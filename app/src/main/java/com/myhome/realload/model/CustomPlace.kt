package com.myhome.realload.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "myPlace")
class CustomPlace {
    @PrimaryKey(autoGenerate = true) var id:Long = 0
    var latitude:Double = 0.0
    var longitude:Double = 0.0
    var name:String = ""
    var favorite:Boolean = false
    override fun toString():String {
        return "id=" + id + "\nlatitude=" + latitude + "\nlongitude=" + longitude + "\nname=" + name + "\nfavorite=" + favorite
    }
}