package com.example.tetrisapp.data.remote;

import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.model.remote.request.InvalidatePlayerDataPayload;
import com.example.tetrisapp.model.remote.request.TokenPayload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GameService {
    @POST("game/start")
    Call<DefaultPayload> startGame(@Body TokenPayload body);

    @POST("game/player/invalidatedata")
    Call<DefaultPayload> invalidatePlayerData(@Body InvalidatePlayerDataPayload body);

    @POST("game/player/lost")
    Call<DefaultPayload> declareLoss(@Body TokenPayload body);
}
