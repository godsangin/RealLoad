package com.myhome.realload.viewmodel.fragment

import com.myhome.realload.model.Place

interface FriendMapListener {
    fun callPlaces(start:String, end:String)
    fun showDatePicker()
}