package com.myhome.realload.viewmodel.fragment

import androidx.lifecycle.LifecycleOwner
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.CustomPlace
import com.myhome.realload.model.Image
import com.myhome.realload.model.NamedPlace

interface PlaceViewModelListener {
    fun addGeofence(place:NamedPlace)
    fun removeGeofence(place:NamedPlace)
    fun goToAlbum(place: NamedPlace?)
    fun getDatabase(): AppDatabase?
    fun getOwner(): LifecycleOwner
    fun updateImages(pid:Long, images:ArrayList<Image>)
}