package com.example.tetrisapp.model.local.model;

public class UserInfo {
    private String name;
    private String photoUrl;
    private String configuration;
    private String uid;

    public UserInfo(String name, String photoUrl, String configuration) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.configuration = configuration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
