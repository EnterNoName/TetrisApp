package com.example.tetrisapp.model.remote.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubmitScoresPayload extends TokenPayload {
    @SerializedName("scores")
    public List<ScorePayload> scores;

    public SubmitScoresPayload(String idToken, List<ScorePayload> scores) {
        super(idToken);
        this.scores = scores;
    }
}
