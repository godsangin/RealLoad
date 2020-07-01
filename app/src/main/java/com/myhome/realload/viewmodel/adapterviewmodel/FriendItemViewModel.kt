package com.myhome.realload.viewmodel.adapterviewmodel

import android.util.Log
import androidx.databinding.ObservableField
import com.myhome.realload.model.Friend
import com.myhome.realload.viewmodel.fragment.FriendListener

class FriendItemViewModel(listener: FriendListener?, friend:Friend) {
    val friend = ObservableField(friend)
    val profileImageUrl = ObservableField<String>(friend.profileUrl)
    val nickName = ObservableField<String>(friend.nickName)
    val buttonVisibility = ObservableField(if(friend.allowedPermission == -2) true else false)
    val listener = listener
    fun showFriendInfo(){
        listener?.showFriendInfo(friend.get()!!)
    }

    fun allowRequest(){
        buttonVisibility.set(false)
        listener?.allowRequest(friend.get()!!)
    }


}