package com.example.tetrisapp.model.remote.response

import com.google.gson.annotations.SerializedName

class LeaderboardData(
    @field:SerializedName("page")
    var currentPage: Int,
    @field:SerializedName("pageCount")
    var pageCount: Int,
    @field:SerializedName("limit")
    var entriesPerPage: Int,
    @field:SerializedName("data")
    var data: List<PublicRecord>
)