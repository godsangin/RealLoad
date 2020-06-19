package com.myhome.realload.viewmodel.fragment

import com.myhome.realload.model.Friend

interface FriendListener {
    fun goSearchFriendActivity()
    fun showFriendInfo(friend:Friend)
}