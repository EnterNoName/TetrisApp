package com.example.tetrisapp.data.remote;

import com.example.tetrisapp.model.remote.Update;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UpdateService {
    @GET("latest")
    Call<Update> getUpdate();
}
