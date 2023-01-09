package com.example.tetrisapp.model.remote.response;

import com.google.gson.annotations.SerializedName;

public class DefaultPayload {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;
}
