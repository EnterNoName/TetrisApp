package com.michaelrayven.tetris_jetpack.feature_game_over.domain.use_case

import com.michaelrayven.tetris_jetpack.feature_game_over.domain.model.Score
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.repository.ScoreRepository

class GetScoreById(
    private val repository: ScoreRepository
) {

    suspend operator fun invoke(id: Long): Score {
        return repository.getScoreById(id)
    }
}