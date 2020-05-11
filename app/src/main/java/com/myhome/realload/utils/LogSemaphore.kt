package com.myhome.realload.utils

class LogSemaphore(semaphore:Boolean) {
    var semaphore = semaphore
    companion object{
        var INSTANCE:LogSemaphore? = null
        @JvmStatic
        fun getInstance():LogSemaphore{
            if(INSTANCE == null){
                INSTANCE = LogSemaphore(false)
            }
            return INSTANCE!!
        }
    }
    fun logFileUse(){
        semaphore = true
    }
    fun useEnd(){
        semaphore = false
    }
}