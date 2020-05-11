package com.myhome.realload.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myhome.realload.model.CustomPlace
import com.myhome.realload.model.Image
import com.myhome.realload.model.Place
import com.myhome.realload.model.PlaceLog

@Database(entities = arrayOf(Place::class, CustomPlace::class, Image::class, PlaceLog::class), version = 1, exportSchema = false)
abstract class AppDatabase :RoomDatabase(){
    abstract fun PlaceDao():PlaceDao
    abstract fun CustomPlaceDao():CustomPlaceDao
    abstract fun ImageDao():ImageDao
    abstract fun LogDao():LogDao

    companion object{
        private var INSTANCE:AppDatabase? = null
        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "place.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}