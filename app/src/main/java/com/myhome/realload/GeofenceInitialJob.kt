package com.myhome.realload

import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.Place
import com.myhome.realload.model.PlaceLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GeofenceInitialJob(context:Context) {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var location: Location

    private val context = context
    private val UPDATE_INTERVAL_MS = 1000.toLong()
    private val FASTEST_UPDATE_INTERVAL_MS = 500.toLong()
    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(context)
    }

    val locationCallback:LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList =
                locationResult.locations
            if (locationList.size > 0) {
                location = locationList[locationList.size - 1]
                //location = locationList.get(0);
                val position = LatLng(location.getLatitude(), location.getLongitude())
                val sharedPreference = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
                val radius = sharedPreference.getFloat("distanceCondition", 10F)
                val geofenceList = mutableListOf<Geofence>(
                    getGeofence("realtimeLocation", Pair(position.latitude, position.longitude), radius))
                val sharedPreferences = context.getSharedPreferences("location", Context.MODE_PRIVATE)
                var startTime = sharedPreferences.getString("startTime", "") ?: ""
                if(startTime.equals("")){
                    addPlace(context, position.latitude, position.longitude)
                }
                addGeofences(geofenceList)
                mFusedLocationClient.removeLocationUpdates(this)
            }
        }
    }


    fun scheduleGeofenceJob(){
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS)
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        startLocationUpdates()

    }


    private fun startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    private fun addGeofences(geofenceList:MutableList<Geofence>) {
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
//        geofencingClient.removeGeofences(mutableListOf("realtimeLocation"))
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
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
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

    fun addPlace(context:Context, latitude:Double, longitude:Double){
        val database = AppDatabase.getInstance(context)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val startTime = format.format(Date())
        val sharedPreferences = context.getSharedPreferences("location", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("startTime", startTime)
        editor.commit()
        CoroutineScope(Dispatchers.IO).launch{
            //            val place = database!!.PlaceDao().selectByLocation(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
            val place = database!!.PlaceDao().selectByStartDate(startTime)
            if(place == null || place.readOnly){//기존에 장소가 없거나 장소에서 떠난적이 있을경우(떠나면 readOnly=true로바뀜)
                val newPlace = Place()
                newPlace.latitude = latitude
                newPlace.longitude = longitude
                newPlace.startDate = startTime
                newPlace.endDate = "..."
                newPlace.readOnly = false
                database.PlaceDao().insert(newPlace)
            }
            else{
            }
            editor.putString("logTime", startTime)
            editor.commit()
            //origin code
            val log = PlaceLog()
            log.latitude = latitude
            log.longitude = longitude
            log.date = startTime
            database!!.LogDao().insert(log)
        }
    }
}