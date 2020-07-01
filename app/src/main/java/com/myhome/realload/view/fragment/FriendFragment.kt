package com.myhome.realload.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonArray
import com.google.gson.JsonObject

import com.myhome.realload.R
import com.myhome.realload.databinding.FragmentFriendBinding
import com.myhome.realload.db.FriendDatabase
import com.myhome.realload.model.Friend
import com.myhome.realload.model.User
import com.myhome.realload.utils.LogSemaphore
import com.myhome.realload.utils.RetrofitAPI
import com.myhome.realload.view.FindFriendActivity
import com.myhome.realload.view.dialog.FriendInfoDialog
import com.myhome.realload.viewmodel.fragment.FriendListener
import com.myhome.realload.viewmodel.fragment.FriendViewModel
import kotlinx.android.synthetic.main.fragment_friend.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class FriendFragment : Fragment() {
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitAPI: RetrofitAPI
    lateinit var viewModel:FriendViewModel
    var fdatabase: FriendDatabase? = null
    var uid:Long = -1
    val friendListener = object:
        FriendListener {
        override fun goSearchFriendActivity() {
            val intent = Intent(context, FindFriendActivity::class.java)
            startActivity(intent)
        }

        override fun showFriendInfo(friend: Friend) {
            //show dialog
            //dialog -> slot function, show location function
            val intent = Intent(context!!, FriendInfoDialog::class.java)
            intent.putExtra("friend", friend)
            startActivity(intent)
        }

        override fun allowRequest(friend: Friend) {
            Log.d("log==","?")
            allowFriendRequest(friend)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel =
            FriendViewModel(friendListener)
        val sharedPreferences = context?.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        fdatabase = FriendDatabase.getInstance(context!!)
        uid = sharedPreferences?.getLong("uid", -1.toLong()) ?: -1.toLong()
        val binding = DataBindingUtil.inflate<FragmentFriendBinding>(inflater, R.layout.fragment_friend, container, false)
        binding.model = viewModel

        if(uid != -1.toLong()){
            setRetrofiInit(context!!)
            getFriendsDataSyn(fdatabase, uid)
            getFriendsRequest(uid)
            viewModel.getFriendsData(fdatabase, viewLifecycleOwner)
        }
        showTxt()
        return binding.root
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

    fun getFriendsDataSyn(fdatabase:FriendDatabase?, uid:Long){
        val apiResult = retrofitAPI.getFriends(uid)
        val retrofitCallback = object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                val result = response.body()
                if((result?.get("responseCode")?.asInt) == 200){
                    val bodyArray = result?.get("body")?.asJsonArray ?: JsonArray()
                    val friends = ArrayList<Friend>()
                    for(userResult in bodyArray){
                        val friend = Friend()
                        friend.nickName = userResult.asJsonObject.get("name").asString
                        friend.allowedPermission = userResult.asJsonObject.get("locationPermission").asInt
                        friend.id = userResult.asJsonObject.get("id").asLong
                        friend.uid = userResult.asJsonObject.get("uid").asLong
                        friends.add(friend)
                    }
                    if(friends != null){
                        CoroutineScope(Dispatchers.IO).launch {
                            for(friend in friends){
                                val existUser = fdatabase?.FriendDao()?.getFriendByUid(friend.uid)
                                if(existUser == null){
                                    fdatabase?.FriendDao()?.insert(friend)
                                }
                                else {
                                    fdatabase?.FriendDao()?.updateFriend(
                                        friend.uid,
                                        "",
                                        friend.tel ?: "",
                                        friend.allowedPermission
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
        apiResult.enqueue(retrofitCallback)
    }

    fun getFriendsRequest(uid:Long){
        val apiResult = retrofitAPI.getFriendRequests(uid)
        val retrofitCallback = object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                val result = response.body()
                Log.d("result==", result.toString())
                if((result?.get("responseCode")?.asInt) == 200){
                    val bodyArray = result?.get("body")?.asJsonArray ?: JsonArray()
                    val friends = ArrayList<Friend>()
                    for(userResult in bodyArray){
                        val friend = Friend()
                        friend.id = userResult.asJsonObject.get("id").asLong
                        friend.nickName = userResult.asJsonObject.get("name").asString
                        friend.allowedPermission = -2
                        friend.uid = userResult.asJsonObject.get("uid").asLong
                        friends.add(friend)
                    }
                    if(friends.size != 0){
                        viewModel.requests.addAll(friends)
                        viewModel.notifyRequestListChanged()
                    }
                }

            }
        }
        apiResult.enqueue(retrofitCallback)
    }

    fun allowFriendRequest(friend:Friend){
//        Log.d("log==", uid.toString() + " " + friend.uid)
        val apiResult = retrofitAPI.allowFriendRequest(uid, friend.uid)
        val retrofitCallback = object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                val result = response.body()
                Log.d("response==", result.toString())
                if((result?.get("responseCode")?.asInt) == 200){
                    CoroutineScope(Dispatchers.IO).launch {
                        friend.allowedPermission = 0
                        fdatabase?.FriendDao()?.insert(friend)
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, context?.getString(R.string.toast_friend_added), Toast.LENGTH_SHORT).show()
                        }
                    }

                }

            }
        }
        apiResult.enqueue(retrofitCallback)
    }

    fun showTxt(){
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
        if(myTxt.exists() == false){
            return
        }
        val inputStream = myTxt.inputStream()
        inputStream.bufferedReader().use { Log.d("log==", it.readText()) }
    }

    companion object {
        private var INSTANCE:FriendFragment? = null
        fun newInstance():FriendFragment?{
            if(INSTANCE == null){
                INSTANCE = FriendFragment()
            }
            return INSTANCE


        }
    }
}
