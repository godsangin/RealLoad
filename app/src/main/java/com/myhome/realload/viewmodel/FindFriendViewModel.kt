package com.myhome.realload.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import com.myhome.realload.R
import com.myhome.realload.model.ApiResponse
import com.myhome.realload.model.Contact
import com.myhome.realload.model.User
import com.myhome.realload.utils.RetrofitAPI
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class FindFriendViewModel(retrofitAPI: RetrofitAPI){
    val contacts = ObservableArrayList<Contact>()
    val dataLoadEnd = ObservableField(false)
    val searchTel = ObservableField("")
    val retrofitAPI = retrofitAPI


    fun getContactList(contentResolver:ContentResolver){ // 너무오래걸림
        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if(cursor == null){
            return
        }
        if(cursor.count > 0){
            while(cursor.moveToNext()){
                val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    val contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                    val pCur = contentResolver.query(
                        contactUri,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )
                    while (pCur?.moveToNext() ?: false) {
                        val phoneType =
                            pCur?.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                        val phoneNumber =
                            pCur?.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val contact = Contact()
                        contact.name = name
                        contact.tel = phoneNumber?.replace("/","")?.replace("\\","")?.replace("+82","0")?.replace("-","") ?:""
                        contacts.add(contact)
                        break

                    }
                }
            }
        }
        dataLoadEnd.set(true)
    }

    fun searchFriend(){
        Log.d("log==", searchTel.get())
        val apiResult = retrofitAPI.getUserByTel(searchTel.get() ?: "")
        val retrofitCallback = object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                val result = response.body()
                if (result?.resultCode == 200) {
                    Log.d("result==", result.toString())
                    val response = ((result.body?.get("user")
                        ?: User()) as User)
                    Log.d("result==", response.toString())
                }
            }
        }
        apiResult.enqueue(retrofitCallback)
    }


}