package com.example.tetrisapp.data.remote;

import com.example.tetrisapp.model.remote.request.CreateLobbyPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.model.remote.request.TokenPayload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LobbyService {
    @POST("lobby/create")
    Call<DefaultPayload> createLobby(@Body CreateLobbyPayload body);

    @POST("lobby/join/{code}")
    Call<DefaultPayload> joinLobby(@Body TokenPayload body, @Path("code") String code);
}
