package com.myhome.realload.viewmodel.fragment

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.myhome.realload.db.FriendDatabase
import com.myhome.realload.model.Friend
import com.myhome.realload.viewmodel.fragment.FriendListener

class FriendViewModel(friendListener: FriendListener) {
    val friends = ObservableArrayList<Friend>()
    var requests = ArrayList<Friend>()
    val friendListener = ObservableField(friendListener)
    fun findFriends(){
        friendListener.get()?.goSearchFriendActivity()
    }

    fun notifyRequestListChanged(){
        requests.addAll(friends)
        friends.clear()
        friends.addAll(requests)
    }

    fun getFriendsData(database:FriendDatabase?, owner: LifecycleOwner){
        if(database == null){
            return
        }
        database.FriendDao().getAllFriend().observe(owner, Observer {
            friends.clear()
            friends.addAll(requests)
            for(friend in it){
                if(friend.allowedPermission == -1){
                    // 응답이 이루어졌는지 확인
                }
                friends.add(friend)
            }
        })
    }
}