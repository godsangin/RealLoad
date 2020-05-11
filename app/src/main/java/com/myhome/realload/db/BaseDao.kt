package com.myhome.realload.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(obj : T)

    @Delete
    fun delete(obj : T)

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(obj : T)
}