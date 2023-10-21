package com.michaelrayven.tetris_jetpack.feature_game_over.presentation

import com.michaelrayven.tetris_jetpack.feature_game_over.domain.model.Score

data class GameOverScreenState(
    val currentScore: Score = Score(0,0,0),
    val highScore: Score = Score(0,0,0),
    val isNewHighScore: Boolean = false
)
