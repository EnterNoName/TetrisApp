package com.example.tetrisapp.model.remote;

import com.google.gson.annotations.SerializedName;

public class DefaultResponse {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;
}
