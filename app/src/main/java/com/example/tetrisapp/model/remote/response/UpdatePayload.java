package com.example.tetrisapp.model.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class UpdatePayload {
    @SerializedName("title")
    public String title;
    @SerializedName("description")
    public String description;
    @SerializedName("type")
    public String type;
    @SerializedName("version")
    public String version;
    @SerializedName("versionId")
    public Integer versionId;
    @SerializedName("url")
    public String url;
    @SerializedName("date")
    public Date date;

    public UpdatePayload(String title, String description, String type, String version, Integer versionId, String url, Date date) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.version = version;
        this.versionId = versionId;
        this.url = url;
        this.date = date;
    }
}
