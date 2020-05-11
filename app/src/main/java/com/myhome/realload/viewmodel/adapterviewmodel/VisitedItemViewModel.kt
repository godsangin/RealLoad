package com.myhome.realload.viewmodel.adapterviewmodel

import androidx.databinding.ObservableField
import com.myhome.realload.model.Place

class VisitedItemViewModel(place:Place, index:Int) {
    val place = ObservableField<Place>(place)
    val index = ObservableField<String>(index.toString())
}