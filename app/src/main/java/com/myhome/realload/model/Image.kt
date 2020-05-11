package com.myhome.realload.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image")
class Image {
    @PrimaryKey(autoGenerate = true) var id:Long = 0
    var pid:Long = 0
    var imageRes:String = ""
}