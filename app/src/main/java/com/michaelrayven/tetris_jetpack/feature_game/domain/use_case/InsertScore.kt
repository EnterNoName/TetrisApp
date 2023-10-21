package com.michaelrayven.tetris_jetpack.feature_game.domain.use_case

import com.michaelrayven.tetris_jetpack.feature_game.domain.repository.GameRepository
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.model.Score
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.repository.ScoreRepository

class InsertScore(
    private val repository: GameRepository
) {

    suspend operator fun invoke(score: Int, lines: Int, level: Int, gameTime: Long, date: Long): Long {
        return repository.insertScore(
            Score(
                score = score,
                lines = lines,
                level = level
            ),
            gameTime,
            date
        )
    }
}