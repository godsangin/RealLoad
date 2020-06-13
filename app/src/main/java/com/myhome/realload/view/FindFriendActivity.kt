package com.myhome.realload.view

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Contacts
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.myhome.realload.R
import com.myhome.realload.databinding.ActivityFindFriendBinding
import com.myhome.realload.model.Contact
import com.myhome.realload.utils.RetrofitAPI
import com.myhome.realload.viewmodel.FindFriendViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Boolean
import java.util.concurrent.TimeUnit


class FindFriendActivity : AppCompatActivity() {
    lateinit var viewModel:FindFriendViewModel
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitAPI: RetrofitAPI


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friend)
        val binding = DataBindingUtil.setContentView<ActivityFindFriendBinding>(this, R.layout.activity_find_friend)
        setRetrofiInit(applicationContext)
        viewModel = FindFriendViewModel(retrofitAPI)
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

}
