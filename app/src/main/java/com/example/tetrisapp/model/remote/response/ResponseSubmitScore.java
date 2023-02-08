package com.example.tetrisapp.model.remote.response;

import com.google.gson.annotations.SerializedName;

public class ResponseSubmitScore {
    @SerializedName("completed")
    public boolean completed;
    @SerializedName("hash")
    public String hash;
}
