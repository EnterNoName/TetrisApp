package com.michaelrayven.tetris_jetpack.feature_game_over.domain.repository

import com.michaelrayven.tetris_jetpack.feature_game_over.domain.model.Score

interface ScoreRepository {

    suspend fun getMaxScore(): Score

    suspend fun getScoreById(id: Long): Score
}