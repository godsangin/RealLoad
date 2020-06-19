package com.myhome.realload.viewmodel.adapterviewmodel

import androidx.databinding.ObservableField
import com.myhome.realload.model.Friend
import com.myhome.realload.viewmodel.fragment.FriendListener

class FriendItemViewModel(listener: FriendListener?, friend:Friend) {
    val friend = friend
    val profileImageUrl = ObservableField<String>(friend.profileUrl)
    val nickName = ObservableField<String>(friend.nickName)
    val listener = listener
    fun showFriendInfo(){
        listener?.showFriendInfo(friend)
    }

    fun addSlot(){

    }
}