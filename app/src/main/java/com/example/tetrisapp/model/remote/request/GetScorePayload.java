package com.example.tetrisapp.model.remote.request;

import com.google.gson.annotations.SerializedName;

public class GetScorePayload extends TokenPayload {
    @SerializedName("page")
    public int page;
    @SerializedName("limit")
    public int limit;

    public GetScorePayload(String idToken, int page, int limit) {
        super(idToken);
        this.page = page;
        this.limit = limit;
    }
}
