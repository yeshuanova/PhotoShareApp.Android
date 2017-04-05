package com.csl.studio.photoshare.app.model;

public class PostItem {
    public String user_uid;
    public String post_content;
    public String thumbnail_name;

    public PostItem(String user_uid, String post_content, String thumbnail_name) {
        this.user_uid = user_uid;
        this.post_content = post_content;
        this.thumbnail_name = thumbnail_name;
    }

}
