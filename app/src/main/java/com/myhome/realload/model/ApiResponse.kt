package com.myhome.realload.model

import org.json.JSONObject

class ApiResponse: JSONObject() {
    var responseCode:Int? = 0
    var body:Map<String, Any>? = null
    override fun toString(): String {
        return responseCode.toString() + body.toString()
    }
}