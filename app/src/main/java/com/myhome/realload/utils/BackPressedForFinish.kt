package com.myhome.realload.utils

import android.app.Activity
import android.widget.Toast

class BackPressedForFinish(activity: Activity, toastMessage:String) {
    var backKeyPressedTime:Long = 0
    var TIME_INTERVAL:Long = 2000
    lateinit var toast:Toast
    var activity = activity
    var toastMessage = toastMessage

    fun onBackPressed(){
        if(System.currentTimeMillis() > backKeyPressedTime + TIME_INTERVAL){
            backKeyPressedTime = System.currentTimeMillis()
            showMessage()
        }
        else{
            toast.cancel()
            activity.finish()
        }
    }

    fun showMessage(){
        toast = Toast.makeText(activity, toastMessage, Toast.LENGTH_LONG)
        toast.show()
    }




}