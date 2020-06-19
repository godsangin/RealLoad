package com.myhome.realload.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myhome.realload.model.Friend

@Database(entities = arrayOf(Friend::class), version = 1, exportSchema = false)
abstract class FriendDatabase :RoomDatabase(){
    abstract fun FriendDao():FriendDao
    companion object{
        private var INSTANCE:FriendDatabase? = null
        fun getInstance(context: Context): FriendDatabase? {
            if (INSTANCE == null) {
                synchronized(FriendDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        FriendDatabase::class.java,
                        "friend.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}