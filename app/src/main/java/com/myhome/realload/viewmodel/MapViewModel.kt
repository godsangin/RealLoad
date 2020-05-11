package com.myhome.realload.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import com.myhome.realload.MapListener
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.NamedPlace
import com.myhome.realload.model.Place
import com.myhome.realload.utils.LocationCirculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MapViewModel(listener:MapListener, database: AppDatabase?, lifecycleOwner: LifecycleOwner):LocationCirculator() {
    val period = ObservableField<Int>()
    val listener = listener
    val database = database
    val lifecycleOwner = lifecycleOwner
    val customDateFrom = ObservableField<String>()
    val customDateTo = ObservableField<String>()
    val logChecked = ObservableField(false)
    init {
        period.set(0)
    }

    fun onClickYesterday(){
        period.set(-1)
        getYesterdayItems()
        logChecked.set(false)
    }
    fun onClickToday(){
        period.set(0)
        getTodayItems()
        logChecked.set(false)
    }

    fun onClickWeek(){
        period.set(1)
        getWeekItems()
        logChecked.set(false)
    }

    fun onClickCustom(){
        period.set(2)
        listener.showDatePicker()
        logChecked.set(false)
    }

    fun getData(start:Date, end:Date){
        val format = SimpleDateFormat("YYYY-MM-dd")

        getData(format.format(start), format.format(end))
    }

    fun getData(start:String, end:String){
        val items = ArrayList<Place>()
        CoroutineScope(Dispatchers.IO).launch {
            val places = database?.PlaceDao()?.selectByDateSyn(start, end)
            if(places == null) return@launch
            for(place in places){
//                val closestPlace = database?.CustomPlaceDao()?.selectByDistance(place.latitude, place.longitude)
                val customPlaces = database?.CustomPlaceDao()?.selectAllSync()
                val closestPlace = findClosestPlace(place, customPlaces)
                if(closestPlace != null && isCloseEnough(place, closestPlace)){
                    val namedPlace = NamedPlace()
                    namedPlace.id = closestPlace.id
                    namedPlace.name = closestPlace.name
                    namedPlace.latitude = place.latitude
                    namedPlace.longitude = place.longitude
                    namedPlace.startDate = place.startDate
                    namedPlace.endDate = place.endDate
                    val images = database?.ImageDao()?.getImagesByPid(namedPlace.id) ?: ArrayList()

                    for(image in images){
                        namedPlace.images.add(image)
                    }
                    items.add(namedPlace)
                }
                else{
                    items.add(place)
                }
            }
            listener.setMarker(items)
        }
    }

    fun getYesterdayItems(){
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DATE, -1)
        getData(calendar.time, Date())
    }

    fun getYesterdayLogs(){
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DATE, -1)
        getLog(calendar.time, Date())
    }

    fun getTodayItems(){
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DATE, 1)
        getData(Date(), calendar.time)
    }

    fun getTodayLogs(){
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DATE, 1)
        getLog(Date(), calendar.time)
    }

    fun getWeekItems(){
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -6)
        val endCal = Calendar.getInstance()
        endCal.add(Calendar.DATE, 1)
        getData(calendar.time, endCal.time)
    }

    fun getWeekLogs(){
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -6)
        val endCal = Calendar.getInstance()
        endCal.add(Calendar.DATE, 1)
        getLog(calendar.time, endCal.time)
    }

    fun getCustomItems(){
        getData(customDateFrom.get() ?: "", customDateTo.get() ?: "")
    }

    fun getCustomLogs(){
        getLog(customDateFrom.get() ?: "", customDateTo.get() ?: "")
    }

    fun refreshData(){
        val case = period.get()
        when(case){
            -1 -> {
                getYesterdayItems()
            }
            0 -> {
                getTodayItems()
            }
            1 -> {
                getWeekItems()
            }
            2 -> {
                getCustomItems()
            }
        }
    }

    fun getLog(start:Date, end:Date){
        val format = SimpleDateFormat("YYYY-MM-dd")

        getLog(format.format(start), format.format(end))
    }

    fun getLog(start:String, end:String){
        CoroutineScope(Dispatchers.IO).launch {
            val logs = database?.LogDao()?.selectByDateSyn(start, end)
            listener.setPolyLine(logs ?: ArrayList())
        }
    }

    fun placeLog(){
        if(!(logChecked.get() ?: false)){
            logChecked.set(true)
            val case = period.get()
            when(case){
                -1 -> {
                    getYesterdayLogs()
                }
                0 -> {
                    getTodayLogs()
                }
                1 -> {
                    getWeekLogs()
                }
                2 -> {
                    getCustomLogs()
                }
            }
        }
        else{
            logChecked.set(false)
            refreshData()
        }
    }
}