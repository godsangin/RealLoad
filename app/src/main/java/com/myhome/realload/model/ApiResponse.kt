package com.myhome.realload.model

import org.json.JSONObject

class ApiResponse: JSONObject() {
    var resultCode:Int? = 0
    var body:Map<String, Any>? = null
    override fun toString(): String {
        return resultCode.toString() + body.toString()
    }
}