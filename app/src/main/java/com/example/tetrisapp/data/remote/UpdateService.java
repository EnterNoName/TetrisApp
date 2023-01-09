package com.example.tetrisapp.data.remote;

import com.example.tetrisapp.model.remote.response.UpdatePayload;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UpdateService {
    @GET("latest")
    Call<UpdatePayload> getUpdate();
}
