package com.michaelrayven.tetris_jetpack.feature_game_over.domain.model

import com.michaelrayven.tetris_jetpack.feature_game_over.data.local.entity.ScoreEntity

data class Score(
    val score: Int,
    val lines: Int,
    val level: Int
) {
    fun toScoreEntity(gameTime: Long, date: Long): ScoreEntity {
        return ScoreEntity(
            score = score,
            lines = lines,
            level = level,
            gameTime = gameTime,
            date = date
        )
    }
}