package com.example.tetrisapp.model.game.singleplayer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class GameOverSingleplayerParcel(
    open var score: Int,
    open var lines: Int,
    open var level: Int,
    open var timer: Int
) : Parcelable