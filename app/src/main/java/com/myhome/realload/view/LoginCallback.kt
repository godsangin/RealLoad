package com.myhome.realload.view

interface LoginCallback{
    fun loginSuccessed(tokenId:Long, nickName:String?, profileUrl:String?)
    fun loginFailed()
}