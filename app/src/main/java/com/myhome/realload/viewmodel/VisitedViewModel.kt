package com.myhome.realload.viewmodel

import android.app.DatePickerDialog
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import com.myhome.realload.FragmentListener
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.NamedPlace
import com.myhome.realload.model.Place
import com.myhome.realload.utils.LocationCirculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class VisitedViewModel(fragmentListener: FragmentListener?, visitedViewModelListener: VisitedViewModelListener, owner: LifecycleOwner, database: AppDatabase?):LocationCirculator() {
    val places = ObservableArrayList<Place>()
    val listener = ObservableField<FragmentListener>()
    val visitedViewModelListener = visitedViewModelListener
    val owner = owner
    val database = database
    val dateText = ObservableField<String>()
    val format = SimpleDateFormat("YYYY-MM-dd")
    var date:Date
    val dataLoadEnd = ObservableField(false)
    val datePickerListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth)
        date = cal.time
        dateText.set(format.format(date))
        getBaseData()
    }

    init {
        listener.set(fragmentListener)
        date = Date()
        dateText.set(format.format(date))
    }
    fun getBaseData(){
        if(database == null) {
            return
        }
        dataLoadEnd.set(false)
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DATE, 1)
        CoroutineScope(Dispatchers.IO).launch {
            val selectedPlaces = database.PlaceDao().selectByDateSyn(dateText.get()!!, format.format(cal.time))
            if(selectedPlaces != null){
                places.clear()
                for(place in selectedPlaces){
                    val customPlaces = database?.CustomPlaceDao()?.selectAllSync()
                    val closestPlace = findClosestPlace(place, customPlaces)
                    if(closestPlace != null && isCloseEnough(place, closestPlace)){
                        val namedPlace = NamedPlace()
                        namedPlace.name = closestPlace.name
                        namedPlace.latitude = place.latitude
                        namedPlace.longitude = place.longitude
                        namedPlace.startDate = place.startDate
                        namedPlace.endDate = place.endDate
                        places.add(namedPlace)
                    }
                    else{
                        places.add(place)
                    }
                }
            }
            dataLoadEnd.set(true)
        }

    }

    fun getNextDate(){
        if(dataLoadEnd.get() == false){
            return
        }
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DATE,1)
        dateText.set(format.format(cal.time))
        date = cal.time
        getBaseData()
    }

    fun getPrevDate(){
        if(dataLoadEnd.get() == false){
            return
        }
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DATE,-1)
        dateText.set(format.format(cal.time))
        date = cal.time
        getBaseData()
    }

    fun createDatePickerDialog(){
        visitedViewModelListener.createDatePicker(datePickerListener)
    }
}