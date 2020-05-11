package com.myhome.realload.viewmodel.adapterviewmodel

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import com.myhome.realload.PlaceListener
import com.myhome.realload.model.Image
import com.myhome.realload.model.NamedPlace

class PlaceRecyclerViewModel(place:NamedPlace, placeListener: PlaceListener?) {
    val place = place
    val placeObserver = ObservableField<NamedPlace>(place)
    val viewPagerType = ObservableField(0)
    val isModifying = ObservableField(false)
    val myImages = ObservableArrayList<Image>()
    val placeListener = placeListener
    val listenerObserver = ObservableField<PlaceListener>(placeListener)

    init {
        myImages.clear()
        myImages.addAll(place.images)
        viewPagerType.set(place.itemViewType)
        if(place.itemViewType == 2){
            modify()
        }

    }

    fun favorite(){
        placeObserver.get()?.let { placeListener?.favorite(it) }
    }

    fun clicked(){
        if(viewPagerType.get() == 0){
            viewPagerType.set(1)
            place.itemViewType = 1
        }
        else{
            place.itemViewType = 0
            viewPagerType.set(0)
        }
    }

    fun modify(){
        viewPagerType.set(2)
        place.itemViewType = 2
        isModifying.set(true)
        myImages.add(Image())
    }

    fun submit(){
        viewPagerType.set(1)
        place.itemViewType = 2
        isModifying.set(false)
        //pid + places 저장하기
        placeListener?.updateImages(myImages)
    }
}