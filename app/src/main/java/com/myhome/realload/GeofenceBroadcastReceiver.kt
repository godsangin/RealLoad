package com.myhome.realload

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import com.google.android.gms.location.*
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.Place
import com.myhome.realload.model.PlaceLog
import com.myhome.realload.utils.GeofencePendingIntent
import com.myhome.realload.utils.LogSemaphore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class GeofenceBroadcastReceiver :BroadcastReceiver(){
    lateinit var geofencingClient: GeofencingClient

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        geofencingClient = LocationServices.getGeofencingClient(context!!)

        if(geofencingEvent.hasError()){
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            return
        }
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val geofenceTransition = geofencingEvent.geofenceTransition
//        saveTxt(context!!, Date().toString() + " " + geofenceTransition + "\n")

        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
            when(geofenceTransition){
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    //Place에 있으면 Noti

                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    LocationServices.getGeofencingClient(context!!)?.removeGeofences(geofencePendingIntent)
                    val format = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
                    saveTxt(context, format.format(Date()))//test
                    updatePlace(context)
                    addPlace(context, geofencingEvent.triggeringLocation.latitude, geofencingEvent.triggeringLocation.longitude)
                    val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
                    val radius = sharedPreferences.getFloat("distanceCondition", 10F)
                    val geofenceList = mutableListOf<Geofence>(
                        getGeofence("realtimeLocation", Pair(geofencingEvent.triggeringLocation.latitude, geofencingEvent.triggeringLocation.longitude), radius))
                    addGeofences(context, geofenceList)
                }
                else -> {

                }
            }
        }else{
        }
    }

    fun updatePlace(context:Context){
        val sharedPreferences = context.getSharedPreferences("location", Context.MODE_PRIVATE)
        val settingSharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val stayCondition = settingSharedPreferences.getLong("stayCondition", 600000)
        var startTime = sharedPreferences.getString("startTime", "")
        val format = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
        if(startTime.equals("")){
            saveTxt(context, "start time null")
            startTime = format.format(Date())
            return
        }
        val database = AppDatabase.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            val place = database!!.PlaceDao().selectByStartDate(startTime!!)
            if(place == null) {
//                saveTxt(context, "start time==" + startTime)
                return@launch
            }
            val start = format.parse(place.startDate)
            try{
                val timeString = format.format(Date())
                val parseTime = format.parse(timeString)
                if((parseTime.time - start.time) < stayCondition && parseTime.time > start.time){
                    saveTxt(context, "removed" + (parseTime.time - start.time).toString())
                    database.PlaceDao().delete(place)//test
                }else{
                    place.readOnly = true
                    place.endDate = format.format(Date())
                    saveTxt(context, "updated")
                    database.PlaceDao().update(place)
                }
            }catch(e:Exception){
                place.readOnly = true
                place.endDate = "Exception interrupt"
                database.PlaceDao().update(place)
            }
        }
    }

    fun addPlace(context:Context, latitude:Double, longitude:Double){
        val database = AppDatabase.getInstance(context)
        val format = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
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
            val log = PlaceLog()
            log.latitude = latitude
            log.longitude = longitude
            log.date = startTime
            database!!.LogDao().insert(log)
        }
    }


    private fun addGeofences(context:Context, geofenceList:MutableList<Geofence>) {
        geofencingClient.addGeofences(getGeofencingRequest(geofenceList), GeofencePendingIntent.getInstance(context).geofencePendingIntent).run {
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

    private fun getGeofence(reqId: String, geo: Pair<Double, Double>, radius: Float = 10f): Geofence {
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
    fun saveTxt(context:Context?, snippet:String){
        val txtSem = LogSemaphore.getInstance()
        if(txtSem.semaphore){
            return
        }
        val file = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            File(context?.dataDir?.path + "/realload")
        }else{
            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/realload")
        }
        if(file?.exists() == false){
            file.mkdir()
        }
        val myTxt = File(file?.path + "/log.txt")
        try{
            txtSem.logFileUse()
            val fos = FileOutputStream(myTxt.path, true)
            val writer = BufferedWriter(OutputStreamWriter(fos))
            writer.write(snippet + "\n")
            writer.flush()
            writer.close()
            fos.close()
            txtSem.useEnd()
        }catch (e:IOException){
            e.printStackTrace()
        }
    }
}