package com.example.tetrisapp.util

import android.content.res.Resources
import android.util.TypedValue

fun convertDpToPixel(resources: Resources, dp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        resources.displayMetrics
    ).toInt()
}