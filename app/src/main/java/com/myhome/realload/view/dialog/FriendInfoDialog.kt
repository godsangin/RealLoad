package com.myhome.realload.view.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.gson.JsonObject
import com.myhome.realload.R
import com.myhome.realload.databinding.DialogFriendInfoBinding
import com.myhome.realload.db.FriendDatabase
import com.myhome.realload.model.Friend
import com.myhome.realload.utils.InterstitialAdController
import com.myhome.realload.utils.RetrofitAPI
import com.myhome.realload.view.FriendLocationActivity
import com.myhome.realload.viewmodel.dialog.FriendInfoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class FriendInfoDialog : Activity() {
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitAPI: RetrofitAPI
    private lateinit var friend:Friend
    private lateinit var viewModel:FriendInfoViewModel

    private lateinit var mInterstitialAd: InterstitialAd
    val friendInfoDialogListener = object:FriendInfoDialogListener{
        override fun allowPermission(permission: Boolean) {
            callPermissionMessage(friend, permission)
        }

        override fun showVisitedInfo() {
            val intent = Intent(applicationContext, FriendLocationActivity::class.java)
            intent.putExtra("friend", friend)
            startActivity(intent)
        }

        override fun showDeniedMessage() {
            Toast.makeText(applicationContext, getString(R.string.toast_location_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_friend_info)

        val intent = getIntent()
        friend = intent.getParcelableExtra<Friend>("friend")

        val binding = DataBindingUtil.setContentView<DialogFriendInfoBinding>(this, R.layout.dialog_friend_info)
        viewModel = FriendInfoViewModel(friend, friendInfoDialogListener)
        binding.model = viewModel
        setRetrofiInit(applicationContext)
        setPermissionAllowed()
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        if(InterstitialAdController.getInstance()?.load() ?: false){
            mInterstitialAd.loadAd(AdRequest.Builder().build())
            mInterstitialAd.show()
        }
    }
    fun setRetrofiInit(context: Context){
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

    fun setPermissionAllowed(){
        val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getLong("uid", -1)
        if(uid == -1.toLong()){
            return
        }
        val apiResult = retrofitAPI.getFriendPermission(friend.uid, uid)
        val retrofitCallback = object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                val result = response.body()
                val responseCode = result?.get("responseCode")?.asInt
                val allowedPermission = result?.get("body")?.asInt
                viewModel.allowedPermission.set(if(allowedPermission==0) false else true)
            }
        }
        apiResult.enqueue(retrofitCallback)
    }

    fun callPermissionMessage(friend:Friend, permission:Boolean){
        val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getLong("uid", -1)
        if(uid == -1.toLong()){
            return
        }
        friend.allowedPermission = if(permission) 1 else 0
        val map = HashMap<String, Any>()
        map.put("id", friend.id)
        map.put("fromUid", uid)
        map.put("toUid", friend.uid)
        map.put("allowedPermission", friend.allowedPermission)
        val apiResult = retrofitAPI.updateFriend(map)
        val retrofitCallback = object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                val result = response.body()
                val responseCode = result?.get("responseCode")?.asInt
                Log.d("result==", responseCode.toString())
                CoroutineScope(Dispatchers.IO).launch {
                    FriendDatabase.getInstance(applicationContext)?.FriendDao()?.update(friend)
                }
            }
        }
        apiResult.enqueue(retrofitCallback)
    }
}