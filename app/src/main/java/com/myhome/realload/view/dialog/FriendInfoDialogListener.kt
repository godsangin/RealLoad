package com.myhome.realload.view.dialog

interface FriendInfoDialogListener {
    fun allowPermission(permission:Boolean)
    fun showVisitedInfo()
    fun showDeniedMessage()

}