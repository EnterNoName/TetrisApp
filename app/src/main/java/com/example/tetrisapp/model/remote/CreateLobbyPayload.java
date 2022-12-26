package com.example.tetrisapp.model.remote;

import com.google.gson.annotations.SerializedName;

public class CreateLobbyPayload extends TokenPayload {
    @SerializedName("countdown")
    public int countdown;

    public CreateLobbyPayload(String idToken, int countdown) {
        super(idToken);
        this.countdown = countdown;
    }
}
