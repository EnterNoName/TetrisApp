package com.example.tetrisapp.model.remote.response;

import com.google.gson.annotations.SerializedName;

public class ScorePayload {
    @SerializedName("uid")
    public String userId;
    @SerializedName("name")
    public String name;
    @SerializedName("score")
    public int score;
    @SerializedName("level")
    public int level;
    @SerializedName("lines")
    public int lines;
    @SerializedName("date")
    public long date;

    public ScorePayload(String userId, String name, int score, int level, int lines, long date) {
        this.userId = userId;
        this.name = name;
        this.score = score;
        this.level = level;
        this.lines = lines;
        this.date = date;
    }
}
