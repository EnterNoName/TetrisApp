package com.example.tetrisapp.model.remote;

import com.google.gson.annotations.SerializedName;

public class StartGamePayload extends TokenPayload {
    @SerializedName("countdown")
    public int countdown;

    public StartGamePayload(String idToken, int countdown) {
        super(idToken);
        this.countdown = countdown;
    }
}
