package com.example.tetrisapp.model.remote.request;

import com.google.gson.annotations.SerializedName;

public class ScorePayload {

    @SerializedName("score")
    public int score;

    @SerializedName("lines")
    public int lines;

    @SerializedName("level")
    public int level;

    @SerializedName("date")
    public long date;

    public ScorePayload(int score, int lines, int level, long date) {
        this.score = score;
        this.lines = lines;
        this.level = level;
        this.date = date;
    }
}
