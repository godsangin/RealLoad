package com.myhome.realload.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceService(context:Context){
    val context = context
    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(context)
    }

    fun startService(){
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastLocation = if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            null
        }else {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
        if(lastLocation == null){
            return
        }
        Log.d("log==", "last=" + lastLocation.latitude + " " + lastLocation.longitude)
        val geofenceList = mutableListOf<Geofence>(getGeofence("realtimeLocation", Pair(lastLocation.latitude, lastLocation.longitude)))
        addGeofences(geofenceList)
    }


    private fun addGeofences(geofenceList:MutableList<Geofence>) {
        geofencingClient.addGeofences(getGeofencingRequest(geofenceList), GeofencePendingIntent.getInstance(context).geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d("log==","added")
            }
            addOnFailureListener {
                Log.d("log==","add failed")
            }
        }
    }


    private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // Geofence 이벤트는 진입시 부터 처리할 때
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)    // Geofence 리스트 추가
        }.build()
    }

    private fun getGeofence(reqId: String, geo: Pair<Double, Double>, radius: Float = 100f): Geofence {
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