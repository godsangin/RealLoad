package com.myhome.realload.viewmodel

import androidx.databinding.ObservableArrayList
import com.myhome.realload.model.Friend

class FriendViewModel(friendListener: FriendListener) {
    val friends = ObservableArrayList<Friend>()
    val friendListener = friendListener
    fun findFriends(){
        friendListener.goSearchFriendActivity()
    }
}