package com.example.tetrisapp.model.remote;

import com.google.gson.annotations.SerializedName;

public class TokenPayload {
    @SerializedName("idToken")
    public String idToken;

    public TokenPayload(String idToken) {
        this.idToken = idToken;
    }
}
