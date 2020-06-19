package com.myhome.realload.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend")
class Friend() :Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0
    var uid:Long = 0
    var nickName:String = ""
    var profileUrl:String = ""
    var tel:String = ""
    var allowedPermission:Int = -1

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        uid = parcel.readLong()
        nickName = parcel.readString().toString()
        profileUrl = parcel.readString().toString()
        tel = parcel.readString().toString()
        allowedPermission = parcel.readInt()
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(id)
        dest?.writeLong(uid)
        dest?.writeString(nickName)
        dest?.writeString(profileUrl)
        dest?.writeString(tel)
        dest?.writeInt(allowedPermission)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return this.id.toString() + " " + this.nickName + " " + this.allowedPermission + " " + this.uid
    }

    companion object CREATOR : Parcelable.Creator<Friend> {
        override fun createFromParcel(parcel: Parcel): Friend {
            return Friend(parcel)
        }

        override fun newArray(size: Int): Array<Friend?> {
            return arrayOfNulls(size)
        }
    }
}