package com.example.tetrisapp.model.remote.response

import com.google.gson.annotations.SerializedName
import java.util.Date

class PersonalRecord(
    @field:SerializedName("uid")
    val uid: String,
    @field:SerializedName("score")
    val score: Int,
    @field:SerializedName("lines")
    val lines: Int,
    @field:SerializedName("level")
    val level: Int,
    @field:SerializedName("timeInGame")
    val timeInGame: Int,
    @field:SerializedName("date")
    val date: Date,
    @field:SerializedName("hash")
    val hash: String,
)