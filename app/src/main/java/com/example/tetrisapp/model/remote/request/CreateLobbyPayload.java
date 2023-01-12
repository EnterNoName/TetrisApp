package com.example.tetrisapp.model.remote.request;

import com.google.gson.annotations.SerializedName;

public class CreateLobbyPayload extends TokenPayload {
    @SerializedName("countdown")
    public int countdown;

    @SerializedName("limit")
    public int playerLimit;

    public CreateLobbyPayload(String idToken, int countdown, int playerLimit) {
        super(idToken);
        this.countdown = countdown;
        this.playerLimit = playerLimit;
    }
}
