package com.example.tetrisapp.data.remote;

import com.example.tetrisapp.model.remote.request.GetScorePayload;
import com.example.tetrisapp.model.remote.request.SubmitScoresPayload;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.model.remote.response.ResponseLeaderboardGet;
import com.example.tetrisapp.model.remote.response.ResponseSubmitScore;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LeaderboardService {
    @POST("leaderboard/submit")
    Call<List<ResponseSubmitScore>> submitScores(@Body SubmitScoresPayload body);

    @POST("leaderboard/get")
    Call<ResponseLeaderboardGet> getScores(@Body GetScorePayload body);

    @POST("leaderboard/placement")
    Call<Integer> getPlacement(@Body TokenPayload body);
}
