package com.myhome.realload.viewmodel

import androidx.databinding.ObservableField

class SettingViewModel(settingViewModelListener: SettingViewModelListener, push:Boolean?, stayCondition:Long?, distanceCondition:Float?) {
    val pushObservableField = ObservableField<Boolean>()
    val stayCondition = ObservableField<Long>()
    val distanceCondition = ObservableField<Float>()
    val stayConditionText = ObservableField<String>()
    val distanceConditionText = ObservableField<String>()
    val settingVisitedViewModelListener = settingViewModelListener
    init{
        if(push == null){
            this.pushObservableField.set(true)
        }
        else{
            this.pushObservableField.set(push)
        }
        if(stayCondition == null){
            this.stayCondition.set(10)
        }
        else{
            this.stayCondition.set(stayCondition)
        }
        if(distanceCondition == null){
            this.distanceCondition.set(10F)
        }
        else{
            this.distanceCondition.set(distanceCondition)
        }

    }

    fun onClickPush(){
        if(pushObservableField.get() == true){
            pushObservableField.set(false)
        }
        else{
            pushObservableField.set(true)
        }
        settingVisitedViewModelListener.setPushNoti(pushObservableField.get()!!)
    }

    fun onClickStayCondition(){
        settingVisitedViewModelListener.showStayConditionDialog()
    }

    fun onClickDistanceCondition(){
        settingVisitedViewModelListener.showDistanceConditionDialog()
    }
}