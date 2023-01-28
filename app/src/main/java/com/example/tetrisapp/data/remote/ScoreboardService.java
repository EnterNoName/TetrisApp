package com.example.tetrisapp.data.remote;

import com.example.tetrisapp.model.remote.request.GetScorePayload;
import com.example.tetrisapp.model.remote.request.SubmitScorePayload;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.model.remote.response.ScorePayload;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ScoreboardService {
    @POST("scoreboard/submit")
    Call<DefaultPayload> submitScore(@Body SubmitScorePayload body);

    @GET("scoreboard/get")
    Call<List<ScorePayload>> getScores(@Body GetScorePayload body);

    @GET("scoreboard/placement")
    Call<Integer> getPlacement(@Body TokenPayload body);

    @GET("scoreboard/pages")
    Call<Integer> getPageCount(@Body TokenPayload body);
}
