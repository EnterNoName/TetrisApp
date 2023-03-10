package com.example.tetrisapp.model.remote.response

import com.google.gson.annotations.SerializedName
import java.util.*

class UpdatePayload(
    @field:SerializedName("title")
    var title: String,
    @field:SerializedName("description")
    var description: String,
    @field:SerializedName("type")
    var type: String,
    @field:SerializedName("version")
    var version: String,
    @field:SerializedName("versionId")
    var versionId: Int,
    @field:SerializedName("url")
    var url: String,
    @field:SerializedName("date")
    var date: Date
)