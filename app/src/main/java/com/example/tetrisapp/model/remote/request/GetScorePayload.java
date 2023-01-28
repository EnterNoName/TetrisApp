package com.example.tetrisapp.model.remote.request;

import com.google.gson.annotations.SerializedName;

public class GetScorePayload extends TokenPayload {
    @SerializedName("page")
    public int page;

    public GetScorePayload(String idToken, int page) {
        super(idToken);
        this.page = page;
    }
}
