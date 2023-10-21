package com.michaelrayven.tetris_jetpack.feature_game_over.domain.use_case

import com.michaelrayven.tetris_jetpack.feature_game_over.domain.repository.ScoreRepository

class GameOverUseCases(
    private val repository: ScoreRepository
) {
    val GetMaxScore = GetMaxScore(repository)
    val GetScoreById = GetScoreById(repository)
}