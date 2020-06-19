package com.myhome.realload.viewmodel.adapterviewmodel

import androidx.databinding.ObservableField
import com.myhome.realload.model.Friend
import com.myhome.realload.viewmodel.FindFriendListener

class ContactItemViewModel(listener:FindFriendListener?, friend:Friend){
    val friend = ObservableField(friend)
    val listener = listener
    fun addFriend(){
        // add friend(fromuid, toUid)
        if(friend.get() == null){
            return
        }
        listener?.addFriend(friend.get()!!)
    }
}