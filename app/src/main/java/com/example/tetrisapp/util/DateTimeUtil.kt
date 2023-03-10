package com.example.tetrisapp.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtil {
    fun toISOString(date: Date): String {
        val tz = TimeZone.getTimeZone("UTC")
        val df: DateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault(Locale.Category.FORMAT)
        )
        df.timeZone = tz
        return df.format(date)
    }
}