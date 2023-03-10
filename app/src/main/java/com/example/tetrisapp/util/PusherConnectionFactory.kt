package com.example.tetrisapp.util

import com.pusher.client.util.UrlEncodedConnectionFactory
import java.net.URLEncoder

class PusherConnectionFactory(
    val token: String,
    val configuration: String?
): UrlEncodedConnectionFactory() {
    override fun getBody(): String {
        return try {
            StringBuilder(super.getBody()).also { params ->
                params.append("&idToken=").append(URLEncoder.encode(token, charset))
                params.append("&configuration=").append(URLEncoder.encode(configuration ?: "DEFAULT", charset))
            }.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}