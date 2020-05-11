package com.myhome.realload.utils

import android.location.Location
import android.util.Log
import com.myhome.realload.model.CustomPlace
import com.myhome.realload.model.Place

open class LocationCirculator {
    fun isCloseEnough(place: Place, closest: CustomPlace):Boolean{
//        val theta = place.longitude - closest.longitude
//        var dist = Math.sin(deg2rad(place.latitude)) * Math.sin(deg2rad(closest.latitude)) +
//                Math.cos(deg2rad(place.latitude)) * Math.cos(deg2rad(closest.latitude)) * Math.cos(deg2rad(theta))
//        dist = Math.acos(dist)
//        dist = rad2deg(dist)
//        dist = dist * 60 * 1.1515 * 1609.0 // 10m단위로 환산
//        Log.d("log==", dist.toString())
//        if(dist < 2){//20m 이하
//            return true
//        }
//        return false
        val location = Location("start")
        location.latitude = place.latitude
        location.longitude = place.longitude
        val location2 = Location("end")
        location2.latitude = closest.latitude
        location2.longitude = closest.longitude
        val distance = location.distanceTo(location2)
        if(distance > 20){
            return false
        }
        return true
    }

    fun findClosestPlace(place:Place, customPlaces:List<CustomPlace>?):CustomPlace?{
        if(customPlaces == null){
            return null
        }
        var distance = Float.MAX_VALUE
        var resultPlace:CustomPlace? = null

        for(customPlace in customPlaces){
            val location = Location("start")
            location.latitude = place.latitude
            location.longitude = place.longitude
            val location2 = Location("end")
            location2.latitude = customPlace.latitude
            location2.longitude = customPlace.longitude
            val distanceNow = location.distanceTo(location2)
            if(distance > distanceNow){
                resultPlace = customPlace
                distance = distanceNow
            }

        }
        return resultPlace
    }

    fun deg2rad(deg:Double):Double{
        return (deg * Math.PI / 180.0)
    }

    fun rad2deg(rad:Double):Double{
        return (rad * 180.0 / Math.PI)
    }
}