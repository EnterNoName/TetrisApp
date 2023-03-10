package com.example.tetrisapp.model.remote.response

import com.google.gson.annotations.SerializedName

class DefaultPayload<T> {
    @SerializedName("success")
    var success: Boolean = false

    @SerializedName("message")
    var message: String = ""

    @SerializedName("errorCode")
    var errorCode: Int? = null

    @SerializedName("data")
    var data: T? = null
}