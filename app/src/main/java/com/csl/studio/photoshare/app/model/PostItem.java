package com.csl.studio.photoshare.app.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class PostItem {
    public String post_uid = "";    // Assign by app, not database structure.
    public String auth_uid = "";
    public String message = ""; // Message Content
    public String photo = "";   // Photo name
    public String thumbnail = "";   // Thumbnail name
    public String time = "";

    public static String AUTH_UID_KEY = "auth_uid";
    public static String MESSAGE_KEY = "message";
    public static String PHOTO_KEY = "photo";
    public static String THUMBNAIL_KEY = "thumbnail";
    public static String POST_TIME_KEY = "time";

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(AUTH_UID_KEY, auth_uid);
        map.put(MESSAGE_KEY, message);
        map.put(PHOTO_KEY, photo);
        map.put(THUMBNAIL_KEY, thumbnail);
        map.put(POST_TIME_KEY, time);
        return map;
    }
}
