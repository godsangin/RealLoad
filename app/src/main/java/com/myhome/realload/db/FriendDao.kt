package com.myhome.realload.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.myhome.realload.model.Friend

@Dao()
interface FriendDao:BaseDao<Friend> {
    @Query("SELECT * FROM friend")
    fun getAllFriendSyn():List<Friend>
    @Query("SELECT * FROM friend")
    fun getAllFriend():LiveData<List<Friend>>

    @Query("UPDATE Friend SET profileUrl=:profileUrl, tel=:tel, allowedPermission=:allowedPermission WHERE uid=:uid")
    fun updateFriend(uid:Long, profileUrl:String, tel:String, allowedPermission:Int)
}
