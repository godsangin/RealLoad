package com.myhome.realload.model

class NamedPlace :Place(){
    var name:String = ""
    var images = ArrayList<Image>()
    var favorite:Boolean = false
    var itemViewType = 0
    override fun equals(other: Any?): Boolean {
        if(id == (other as NamedPlace).id){
            return true
        }
        return false
    }
}