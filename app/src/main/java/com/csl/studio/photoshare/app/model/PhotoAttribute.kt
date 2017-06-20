package com.csl.studio.photoshare.app.model

import java.util.ArrayList
import java.util.HashMap

class PhotoAttribute {

    var ext = ""
    var upload_time = ""
    var tags: List<String> = ArrayList()


    fun toMap(): Map<String, Any> {
        val maps = HashMap<String, Any>()
        maps.put(FILE_EXT_KEY, ext)
        maps.put(TAGS_KEY, tags)
        maps.put(UPLOAD_TIME_KEY, upload_time)
        return maps
    }

    companion object {

        val FILE_EXT_KEY = "ext"
        val UPLOAD_TIME_KEY = "upload_time"
        val TAGS_KEY = "tags"
    }

}
