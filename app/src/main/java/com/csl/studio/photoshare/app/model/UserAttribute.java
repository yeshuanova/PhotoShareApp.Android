package com.csl.studio.photoshare.app.model;


import java.util.HashMap;
import java.util.Map;

public class User {

    public String name = "";
    public String email = "";

    public Map<String, Object> toMap() {
        HashMap<String, Object> maps = new HashMap<>();
        maps.put("name", this.name);
        maps.put("email", this.email);
        return maps;
    }

}
