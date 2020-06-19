package com.myhome.realload.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonArray
import com.google.gson.JsonObject

import com.myhome.realload.R
import com.myhome.realload.databinding.FragmentFriendBinding
import com.myhome.realload.db.FriendDatabase
import com.myhome.realload.model.Friend
import com.myhome.realload.model.User
import com.myhome.realload.utils.RetrofitAPI
import com.myhome.realload.view.FindFriendActivity
import com.myhome.realload.view.dialog.FriendInfoDialog
import com.myhome.realload.viewmodel.fragment.FriendListener
import com.myhome.realload.viewmodel.fragment.FriendViewModel
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

class FriendFragment : Fragment() {
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitAPI: RetrofitAPI
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val viewModel =
            FriendViewModel(friendListener)
        val sharedPreferences = context?.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val fdatabase = FriendDatabase.getInstance(context!!)
        val uid = sharedPreferences?.getLong("uid", -1.toLong()) ?: -1.toLong()
        val binding = DataBindingUtil.inflate<FragmentFriendBinding>(inflater, R.layout.fragment_friend, container, false)
        binding.model = viewModel
//        for(i in 0..3){
//            val friend = Friend()
//            friend.nickName = "이상인"
//            friend.profileUrl = ""
//            viewModel.friends.add(friend)
//        }
        viewModel.getFriendsData(fdatabase, viewLifecycleOwner)
        if(uid != -1.toLong()){
            setRetrofiInit(context!!)
            getFriendsDataSyn(fdatabase, uid)
        }
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
                    val users = ArrayList<User>()
                    for(userResult in bodyArray){
                        val user = User()
                        user.name = userResult.asJsonObject.get("name").asString
                        user.locationPermission = userResult.asJsonObject.get("locationPermission").asInt
                        user.id = userResult.asJsonObject.get("id").asLong
                        users.add(user)
                    }
                    if(users != null){
                        CoroutineScope(Dispatchers.IO).launch {
                            for(user in users){
                                fdatabase?.FriendDao()?.updateFriend(user.id,"", user.tel ?:"", user.locationPermission)
                            }
                        }
                    }
                }

            }
        }
        apiResult.enqueue(retrofitCallback)
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
