package com.myhome.realload.model

import org.json.JSONArray
import org.json.JSONObject

class ApiArrayResponse:JSONObject() {
    var responseCode:Int? = 0
    var body:JSONArray? = null
    override fun toString(): String {
        return responseCode.toString() + body.toString()
    }
}