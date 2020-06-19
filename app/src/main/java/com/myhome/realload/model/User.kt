package com.myhome.realload.model

class User {
    var id: Long = -1
    var tokenId: Long = 0
    var tel: String? = null
    var name: String? = null
    var activeDate: String? = null
    var locationPermission = 0
    var profileUrl:String? = null

    companion object {
        fun getInstance(map: Map<String, Any>?): User {
            val user = User()
            if(map == null){
                return user
            }
            user.id = (map.get("id") as Double).toLong()
            user.tokenId = (map.get("tokenId") as Double).toLong()
            user.tel = (map.get("tel") as String)
            user.name = map.get("name") as String
            user.activeDate = map.get("activeDate") as String
            user.locationPermission = (map.get("locationPermission") as Double).toInt()
            return user
        }
    }
}