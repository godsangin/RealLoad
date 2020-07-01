package com.myhome.realload.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import com.myhome.realload.R
import com.myhome.realload.databinding.ActivityFindFriendBinding
import com.myhome.realload.db.FriendDatabase
import com.myhome.realload.model.ApiResponse
import com.myhome.realload.model.Friend
import com.myhome.realload.utils.RetrofitAPI
import com.myhome.realload.viewmodel.FindFriendListener
import com.myhome.realload.viewmodel.FindFriendViewModel
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


class FindFriendActivity : AppCompatActivity() {
    lateinit var viewModel:FindFriendViewModel
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitAPI: RetrofitAPI
    val findFriendListener = object:FindFriendListener{
        override fun addFriend(friend: Friend) {
            //insertFriend
            val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
            val currentUid = sharedPreferences.getLong("uid", -1.toLong())
            //uid + currentUid 조합으로 통신
            if(currentUid == friend.uid){
                Toast.makeText(applicationContext, getString(R.string.toast_cannot_request_myself), Toast.LENGTH_SHORT).show()
                return
            }
            insertFriend(currentUid, friend)
        }

        override fun showNetworkEnabledToast() {
            Toast.makeText(applicationContext, getString(R.string.toast_network_enabled), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friend)
        val binding = DataBindingUtil.setContentView<ActivityFindFriendBinding>(this, R.layout.activity_find_friend)
        setRetrofiInit(applicationContext)
        viewModel = FindFriendViewModel(findFriendListener, retrofitAPI, FriendDatabase.getInstance(applicationContext))
        binding.model = viewModel
//        viewModel.getContactList(contentResolver)
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

    fun insertFriend(currentUid:Long, friend:Friend){
        val map = HashMap<String, Long>()
        map.put("fromUid", currentUid)
        map.put("toUid", friend.uid)
        val apiResult = retrofitAPI.insertFriend(map)
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
                if (responseCode == 200) {
                    val fromFriendId = result?.get("fromId")?.asLong
                    val toFriendId = result?.get("toId")?.asLong
                    friend.id = fromFriendId ?:-1.toLong()
                    Log.d("friendID==", friend.id.toString())
                    val fdatabase = FriendDatabase.getInstance(applicationContext)
                    CoroutineScope(Dispatchers.IO).launch {
                        fdatabase?.FriendDao()?.insert(friend)
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(applicationContext, getString(R.string.toast_send_friend_request), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        apiResult.enqueue(retrofitCallback)

    }

}
