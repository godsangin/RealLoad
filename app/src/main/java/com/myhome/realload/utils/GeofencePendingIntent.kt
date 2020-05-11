package com.myhome.realload.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.myhome.realload.GeofenceBroadcastReceiver

class GeofencePendingIntent(context: Context) {
    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    companion object{
        var INSTANCE:GeofencePendingIntent? = null
        @JvmStatic
        fun getInstance(context:Context):GeofencePendingIntent{
            if(INSTANCE == null){
                INSTANCE = GeofencePendingIntent(context)
            }
            return INSTANCE!!
        }
        @JvmStatic
        fun getInstance():GeofencePendingIntent?{
            return INSTANCE
        }
    }
}