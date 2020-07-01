package com.myhome.realload.utils

class InterstitialAdController(count:Int){
    var count = count
    companion object{
        private var INSTANCE: InterstitialAdController? = null
        fun getInstance(): InterstitialAdController? {
            if (INSTANCE == null) {
                synchronized(InterstitialAdController::class) {
                    INSTANCE = InterstitialAdController(1)
                }
            }
            return INSTANCE
        }
    }
    fun load():Boolean{
        if(count == 3){
            count = 0
            return true
        }
        count++
        return false
    }
}