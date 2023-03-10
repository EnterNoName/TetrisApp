package com.example.tetrisapp.model.game.multiplayer

import com.example.tetrisapp.model.game.singleplayer.GameOverSingleplayerParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class GameOverMultiplayerParcel(
    override var score: Int,
    override var lines: Int,
    override var level: Int,
    override var timer: Int,
    var placement: Int,
    var inviteCode: String,
    var winnerUserId: String
) : GameOverSingleplayerParcel(score, lines, level, timer)