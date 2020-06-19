package com.myhome.realload.viewmodel

import com.myhome.realload.model.Friend

interface FindFriendListener {
    fun addFriend(friend:Friend)
    fun showNetworkEnabledToast()
}