package com.myhome.realload.model

class ApplicationUser{
    companion object{
        private var INSTANCE:User? = null
        fun setInstance(user:User){
            INSTANCE = user
        }
        fun getInstance():User?{
            return INSTANCE
        }
    }
}