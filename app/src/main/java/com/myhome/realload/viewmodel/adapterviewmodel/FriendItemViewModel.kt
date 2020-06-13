package com.myhome.realload.viewmodel.adapterviewmodel

import androidx.databinding.ObservableField
import com.myhome.realload.model.Friend

class FriendItemViewModel(friend:Friend) {
    val profileImageUrl = ObservableField<String>(friend.profileUrl)
    val nickName = ObservableField<String>(friend.nickName)


    fun addSlot(){

    }
}