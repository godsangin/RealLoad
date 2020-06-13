package com.myhome.realload

import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.CustomPlace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomGeofenceService :JobService(){
    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(applicationContext)
    }
    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(applicationContext, CustomGeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        removeGeofenceJobs()

        val sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val push = sharedPreferences.getBoolean("push", true)
        if(push){
            scheduleGeofenceJobs()
        }
        return true
    }

    private fun scheduleGeofenceJobs(){
        val database = AppDatabase.getInstance(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            val places = database?.CustomPlaceDao()?.selectFavoritePlace()
            if(places == null){
                return@launch
            }
            addGeofence(places)

        }
    }

    fun removeGeofenceJobs(){
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    private fun addGeofence(places:List<CustomPlace>){

        val sharedPreference = applicationContext?.getSharedPreferences("setting", Context.MODE_PRIVATE)
        var radius = sharedPreference?.getFloat("distanceCondition", 20F)
        val geofenceList = mutableListOf<Geofence>()
        for(place in places){
            geofenceList.add(getGeofence(place.id.toString(), Pair(place.latitude, place.longitude), radius!!))
        }
        geofencingClient.addGeofences(getGeofencingRequest(geofenceList), geofencePendingIntent).run {
            addOnSuccessListener {
            }
            addOnFailureListener {
            }
        }
    }

    private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // Geofence 이벤트는 진입시 부터 처리할 때
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
            addGeofences(list)    // Geofence 리스트 추가
        }.build()
    }

    private fun getGeofence(reqId: String, geo: Pair<Double, Double>, radius: Float): Geofence {
        return Geofence.Builder()
            .setRequestId(reqId)    // 이벤트 발생시 BroadcastReceiver에서 구분할 id
            .setCircularRegion(geo.first, geo.second, radius)    // 위치 및 반경(m)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)        // Geofence 만료 시간
            .setLoiteringDelay(10000)                            // 머물기 체크 시간
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER                // 진입 감지시
                        or Geofence.GEOFENCE_TRANSITION_EXIT    // 이탈 감지시
                        or Geofence.GEOFENCE_TRANSITION_DWELL)    // 머물기 감지시
            .build()
    }

}