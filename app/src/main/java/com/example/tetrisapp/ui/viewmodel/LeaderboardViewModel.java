package com.example.tetrisapp.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.tetrisapp.ui.adapters.ScoresRecyclerViewAdapter;
import com.example.tetrisapp.util.FirebaseTokenUtil;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardViewModel extends ViewModel {
    private String token;
    private final List<ScoresRecyclerViewAdapter.Score> scores = new ArrayList<>();

    public LeaderboardViewModel() {
    }

    public List<ScoresRecyclerViewAdapter.Score> getScores() {
        return scores;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
