package com.csl.studio.photoshare.app.model


import java.util.HashMap

class UserAttribute {

    var name = ""
    var email = ""

    fun toMap(): Map<String, Any> {
        val maps = HashMap<String, Any>()
        maps.put("name", this.name)
        maps.put("email", this.email)
        return maps
    }

}
