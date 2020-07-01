package com.myhome.realload

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.myhome.realload.model.ApiResponse
import com.myhome.realload.model.Place
import com.myhome.realload.utils.LogSemaphore
import com.myhome.realload.utils.RetrofitAPI
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.util.concurrent.TimeUnit


class GeofenceForegroundService : Service(){

    private lateinit var retrofit: Retrofit
    private lateinit var retrofitAPI: RetrofitAPI

    override fun onBind(intent: Intent?): IBinder? {
        saveTxt(applicationContext, "foreground service binded")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        saveTxt(applicationContext, "foreground service excuted")
        startForegroundService(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        setRetrofiInit(applicationContext)
        saveTxt(applicationContext, "foreground service onstartcommand")
        val place = Place()
        val uid = intent?.getLongExtra("uid", -1) ?: -1.toLong()
        place.latitude = intent?.getDoubleExtra("latitude", -1.toDouble()) ?: -1.toDouble()
        place.longitude = intent?.getDoubleExtra("longitude", -1.toDouble()) ?: -1.toDouble()
        place.startDate = intent?.getStringExtra("startDate") ?:"null"
        place.endDate = intent?.getStringExtra("endDate") ?:"null"
        doPlaceInsert(applicationContext, uid, place)
        return super.onStartCommand(intent, flags, startId)
    }


    fun startForegroundService(context: Context){
        lateinit var builder:NotificationCompat.Builder
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                val CHANNEL_ID = "realtimeLocation"
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "realtimeLocation",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(channel)
                builder = NotificationCompat.Builder(this, CHANNEL_ID)
            } else {
                builder = NotificationCompat.Builder(this)
            }

            builder.setSmallIcon(R.drawable.logo_realload)
            builder.setContentTitle(context.getString(R.string.app_name))
            builder.setContentText(context.getString(R.string.noti_location_changed))

            val intent = Intent(context, GeofenceForegroundService::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            builder.setContentIntent(pendingIntent)


            startForeground(1, builder.build())
        }catch(e:Exception){
            saveTxt(applicationContext, e.message ?:"null")
        }
    }

    fun setRetrofiInit(context:Context){
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS).build()
        retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.apiUrl))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitAPI = retrofit.create(RetrofitAPI::class.java)
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

    fun doPlaceInsert(context:Context, uid:Long, place: Place){
        val apiResult = retrofitAPI.insertPlace(uid, place)
        val retrofitCallback = object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                t.printStackTrace()
                saveTxt(context,t.message ?: "null")
                Toast.makeText(context, context.getString(R.string.toast_network_enabled), Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                val result = response.body()
                if (result?.responseCode == 200) {
                    stopSelf()
                }
                else{
                    saveTxt(context, (result ?: "null").toString())
                    Toast.makeText(context, context.getString(R.string.toast_network_enabled), Toast.LENGTH_SHORT).show()
                }
            }
        }
        apiResult.enqueue(retrofitCallback)
    }
}