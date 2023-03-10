package com.example.tetrisapp.model.local.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class GameStartedData(
    val countdown: Int,
    val gameMode: String,
    val timer: Int,
    val enablePause: Boolean,
    val playerList: MutableList<String>
): Parcelable