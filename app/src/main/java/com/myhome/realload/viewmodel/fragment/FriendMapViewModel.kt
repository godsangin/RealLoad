package com.myhome.realload.viewmodel.fragment

import androidx.databinding.ObservableField
import com.myhome.realload.utils.LocationCirculator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FriendMapViewModel(listener:FriendMapListener):LocationCirculator() {
    val period = ObservableField<Int>()
    val customDateFrom = ObservableField<String>()
    val customDateTo = ObservableField<String>()
    val listener = listener
    val logChecked = ObservableField(false)
    init {
        period.set(-2)
//        refreshData()
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
        val format = SimpleDateFormat("yyyy-MM-dd")
        getData(format.format(start), format.format(end))
    }

    fun getData(start:String, end:String){
        listener.callPlaces(start,end)
    }

    fun getYesterdayItems(){
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DATE, -1)
        getData(calendar.time, Date())
    }

    fun getTodayItems(){
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DATE, 1)
        getData(Date(), calendar.time)
    }


    fun getWeekItems(){
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -6)
        val endCal = Calendar.getInstance()
        endCal.add(Calendar.DATE, 1)
        getData(calendar.time, endCal.time)
    }


    fun getCustomItems(){
        getData(customDateFrom.get() ?: "", customDateTo.get() ?: "")
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

}