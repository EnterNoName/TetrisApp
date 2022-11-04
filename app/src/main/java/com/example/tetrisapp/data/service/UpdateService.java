package com.example.tetrisapp.data.service;

import com.example.tetrisapp.model.Update;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UpdateService {
    @GET("latest")
    Call<Update> getUpdate();
}
