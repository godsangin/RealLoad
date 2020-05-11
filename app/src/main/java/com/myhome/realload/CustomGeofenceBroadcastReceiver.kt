package com.myhome.realload

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.Place
import com.myhome.realload.utils.LocationCirculator
import com.myhome.realload.utils.LogSemaphore
import com.myhome.realload.view.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

class CustomGeofenceBroadcastReceiver :BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        Log.d("log==", "excuted")
        if(geofencingEvent.hasError()){
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val location = geofencingEvent.triggeringLocation

        CoroutineScope(Dispatchers.IO).launch {
//            val place = AppDatabase.getInstance(context!!)?.CustomPlaceDao()?.selectFavoriteByDistance(location.latitude, location.longitude)
            val place = Place()
            place.latitude = location.latitude
            place.longitude = location.longitude
            val customPlaces = AppDatabase.getInstance(context!!)?.CustomPlaceDao()?.selectFavoritePlace()
//            saveTxt(context, "log== "+ location.latitude.toString() + "\n" + location.longitude)
            val locationCirculator = LocationCirculator()
            val geofencePlace = locationCirculator.findClosestPlace(place, customPlaces)
//            saveTxt(context, geofencePlace?.name ?:"null")
            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
                when(geofenceTransition){
                    Geofence.GEOFENCE_TRANSITION_DWELL -> {
                        //Place에 있으면 Noti
                        sendNotification(context, context.getString(R.string.app_name), geofencePlace?.name + " " + context.getString(R.string.noti_enter_place_description))
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        sendNotification(context, context.getString(R.string.app_name), geofencePlace?.name + " " + context.getString(R.string.noti_exit_place_description))
                    }
                    else -> {

                    }
                }
            }else{

            }
        }

    }


    private fun sendNotification(context:Context, title:String, content:String) {
        var title: String? = title
        val sharedPreferences =
            context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val push = sharedPreferences.getBoolean("push", true)
        if (!push) {
            return
        }
        if (title == null) title = "기본 제목"
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        // 오레오(8.0) 이상일 경우 채널을 반드시 생성해야 한다.
        val CHANNEL_ID = context.getString(R.string.default_notification_channel_id)
        val mManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_NAME = "fcm messaging"
            val CHANNEL_DESCRIPTION = ""
            val importance = NotificationManager.IMPORTANCE_HIGH
            // add in API level 26
            val mChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            mChannel.description = CHANNEL_DESCRIPTION
            mChannel.enableLights(true)
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 100, 200)
            mChannel.setSound(defaultSoundUri, null)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            mManager.createNotificationChannel(mChannel)
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setAutoCancel(true)
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.drawable.ic_launcher_noti)
        builder.setContentText(content)
        builder.setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { // 아래 설정은 오레오부터 deprecated 되면서 NotificationChannel에서 동일 기능을 하는 메소드를 사용.
            builder.setContentTitle(title)
            builder.setSound(defaultSoundUri)
            builder.setVibrate(longArrayOf(500, 500))
        }
        mManager.notify(0, builder.build())
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
        }catch (e: IOException){
            e.printStackTrace()
        }
    }
}