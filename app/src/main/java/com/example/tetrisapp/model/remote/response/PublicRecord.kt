package com.example.tetrisapp.model.remote.response

import com.google.gson.annotations.SerializedName
import java.util.*

class PublicRecord(
    @field:SerializedName("uid")
    var userId: String,
    @field:SerializedName("name")
    var name: String,
    @field:SerializedName("score")
    var score: Int,
    @field:SerializedName("level")
    var level: Int,
    @field:SerializedName("lines")
    var lines: Int,
    @field:SerializedName("date")
    var date: Date
)