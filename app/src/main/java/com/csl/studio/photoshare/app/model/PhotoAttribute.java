package com.csl.studio.photoshare.app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoAttribute {

    public String file_ext = "";
    public String upload_time = "";
    public List<String> tags = new ArrayList<>();

    public static final String FILE_EXT_KEY = "ext";
    public static final String UPLOAD_TIME_KEY = "upload_time";
    public static final String TAGS_KEY = "tags";


    public Map<String, Object> toMap() {
        HashMap<String, Object> maps = new HashMap<>();
        maps.put(FILE_EXT_KEY, file_ext);
        maps.put(TAGS_KEY, tags);
        maps.put(UPLOAD_TIME_KEY, upload_time);
        return maps;
    }

}
