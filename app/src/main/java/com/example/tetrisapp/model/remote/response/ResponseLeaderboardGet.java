package com.example.tetrisapp.model.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseLeaderboardGet {
    @SerializedName("page")
    public int currentPage;
    @SerializedName("pageCount")
    public int pageCount;
    @SerializedName("limit")
    public int entriesPerPage;
    @SerializedName("data")
    public List<ResponseLeaderboardEntry> data;

    public ResponseLeaderboardGet(int currentPage, int pageCount, int entriesPerPage, List<ResponseLeaderboardEntry> data) {
        this.currentPage = currentPage;
        this.pageCount = pageCount;
        this.entriesPerPage = entriesPerPage;
        this.data = data;
    }
}
