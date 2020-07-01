package com.myhome.realload

import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.os.PersistableBundle
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.ApiResponse
import com.myhome.realload.model.Place
import com.myhome.realload.model.PlaceLog
import com.myhome.realload.utils.LogSemaphore
import com.myhome.realload.utils.RetrofitAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GeofenceService:JobService() {
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitAPI: RetrofitAPI
    private var mParams: JobParameters? = null

    override fun onStopJob(params: JobParameters?): Boolean {
        saveTxt(applicationContext, "job stoped")
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        mParams = params
        setRetrofiInit(applicationContext)
        val extras = params?.extras ?: PersistableBundle()
        val place = Place()
        val uid = extras.getLong("uid")
        place.latitude = extras.getDouble("latitude")
        place.longitude = extras.getDouble("longitude")
        place.startDate = extras.getString("startDate") ?:"null"
        place.endDate = extras.getString("endDate") ?:"null"
        saveTxt(applicationContext, "Job excuted")
        Thread{
            doPlaceInsert(applicationContext, uid, place)
        }.start()
        return true
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


    fun doPlaceInsert(context:Context, uid:Long, place:Place){
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
                    jobFinished(mParams, false)
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