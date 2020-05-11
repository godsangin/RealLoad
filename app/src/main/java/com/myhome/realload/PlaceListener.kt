package com.myhome.realload

import androidx.lifecycle.LifecycleOwner
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.CustomPlace
import com.myhome.realload.model.Image
import com.myhome.realload.model.NamedPlace

interface PlaceListener{
    fun favorite(place: NamedPlace)
    fun goToAlbum(place: NamedPlace?)
    fun getDatabase():AppDatabase?
    fun getOwner():LifecycleOwner
    fun updateImages(images:ArrayList<Image>)
    fun removeItem(place:NamedPlace?, position:Int)
}