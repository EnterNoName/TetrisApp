package com.michaelrayven.tetris_jetpack.feature_game_over.data.repository

import com.michaelrayven.tetris_jetpack.feature_game_over.data.local.ScoreDao
import com.michaelrayven.tetris_jetpack.feature_game_over.data.local.entity.ScoreEntity
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.model.Score
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.repository.ScoreRepository

class ScoreRepositoryImpl(
    private val dao: ScoreDao
): ScoreRepository {
    override suspend fun getMaxScore(): Score {
        return dao.getMaxScore()?.toScore() ?: Score(0, 0, 0)
    }

    override suspend fun getScoreById(id: Long): Score {
        return dao.getById(id)?.toScore() ?: Score(0, 0, 0)
    }
}