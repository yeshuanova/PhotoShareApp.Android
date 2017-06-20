package com.csl.studio.photoshare.app.model

import com.google.firebase.database.Exclude

import java.util.HashMap

class PostItem {
    var post_uid = ""    // Assign by app, not database structure.
    var auth_uid = ""
    var message = "" // Message Content
    var photo = ""   // Photo name
    var thumbnail = ""   // Thumbnail name
    var time = ""

    @Exclude
    fun toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map.put(AUTH_UID_KEY, auth_uid)
        map.put(MESSAGE_KEY, message)
        map.put(PHOTO_KEY, photo)
        map.put(THUMBNAIL_KEY, thumbnail)
        map.put(POST_TIME_KEY, time)
        return map
    }

    companion object {

        var AUTH_UID_KEY = "auth_uid"
        var MESSAGE_KEY = "message"
        var PHOTO_KEY = "photo"
        var THUMBNAIL_KEY = "thumbnail"
        var POST_TIME_KEY = "time"
    }
}
