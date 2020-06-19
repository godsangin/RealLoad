package com.myhome.realload.viewmodel

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import com.myhome.realload.model.ApiResponse
import com.myhome.realload.model.Friend
import com.myhome.realload.model.User
import com.myhome.realload.utils.RetrofitAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindFriendViewModel(listner:FindFriendListener, retrofitAPI: RetrofitAPI){
    val friends = ObservableArrayList<Friend>()
    val dataLoadEnd = ObservableField(false)
    val searchTel = ObservableField("")
    val retrofitAPI = retrofitAPI
    val listener = ObservableField(listner)


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
                        val friend = Friend()
                        friend.nickName = name
                        friend.tel = phoneNumber?.replace("/","")?.replace("\\","")?.replace("+82","0")?.replace("-","") ?:""
                        friends.add(friend)
                        break

                    }
                }
            }
        }
        dataLoadEnd.set(true)
    }

    fun searchFriend(){
        friends.clear()
        dataLoadEnd.set(false)
        val apiResult = retrofitAPI.getUserByTel(searchTel.get() ?: "")
        val retrofitCallback = object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                t.printStackTrace()
                listener.get()?.showNetworkEnabledToast()
            }

            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                val result = response.body()
                if (result?.responseCode == 200) {
                    Log.d("result==", result.toString())
                    val response = User.getInstance(result.body?.get("user") as Map<String, Any>)
                    if(response.id == -1.toLong()){
                        //메시지나 카톡으로 앱 깔기 유도
                        dataLoadEnd.set(true)
                        return
                    }
                    val friend = Friend()
                    friend.nickName = response.name ?: ""
                    friend.tel = response.tel ?: ""
                    friend.uid = response.id
                    //profileSource?
                    friends.add(friend)
                    dataLoadEnd.set(true)
                }
                else{
                    listener.get()?.showNetworkEnabledToast()
                }
            }
        }
        apiResult.enqueue(retrofitCallback)
    }


}