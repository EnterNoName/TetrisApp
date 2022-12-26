package com.example.tetrisapp.data.remote;

import com.example.tetrisapp.model.remote.CreateLobbyPayload;
import com.example.tetrisapp.model.remote.DefaultResponse;
import com.example.tetrisapp.model.remote.TokenPayload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LobbyService {
    @POST("lobby/create")
    Call<DefaultResponse> createLobby(@Body CreateLobbyPayload body);

    @POST("lobby/join/{code}")
    Call<DefaultResponse> joinLobby(@Body TokenPayload body, @Path("code") String code);

    @POST("lobby/exit")
    Call<DefaultResponse> exitLobby(@Body TokenPayload body);
}
