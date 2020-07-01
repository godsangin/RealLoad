package com.myhome.realload.viewmodel.dialog

import androidx.databinding.ObservableField
import com.myhome.realload.model.Friend
import com.myhome.realload.view.dialog.FriendInfoDialogListener

class FriendInfoViewModel(friend:Friend, listener:FriendInfoDialogListener) {
    val friend = ObservableField(friend)
    val allowPermission = ObservableField(if(friend.allowedPermission==1) true else false)
    val allowedPermission = ObservableField(false)
    val listener = listener

    fun checkAllowButton(){
        if(allowPermission.get() ?: true){
            //deny
            listener.allowPermission(false)
            allowPermission.set(false)
        }
        else{
            //allow
            allowPermission.set(true)
            listener.allowPermission(true)
        }
    }

    fun showFriendVisitedInfo(){
        if(!(allowedPermission.get() ?:false)){
            //toast -> 친구가 나에게 권한을 허용하지 않았어요..
            listener.showDeniedMessage()
        }
        else{
            //go friendInfoActivity
            listener.showVisitedInfo()
        }
    }
}