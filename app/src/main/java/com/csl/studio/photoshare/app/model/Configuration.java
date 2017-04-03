package com.csl.studio.photoshare.app.model;


import com.google.firebase.auth.FirebaseAuth;

public class Configuration {

    private static Configuration _config;

    private FirebaseAuth _auth_info;

    public Configuration getInstance() {
        if (null == _config) {
            _config = new Configuration();
        }
        return _config;
    }

}
