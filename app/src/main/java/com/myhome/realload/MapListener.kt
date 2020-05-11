package com.myhome.realload

import com.myhome.realload.model.Place
import com.myhome.realload.model.PlaceLog


interface MapListener{
    fun setMarker(places:ArrayList<Place>)
    fun showDatePicker()
    fun setPolyLine(logs:List<PlaceLog>)
}