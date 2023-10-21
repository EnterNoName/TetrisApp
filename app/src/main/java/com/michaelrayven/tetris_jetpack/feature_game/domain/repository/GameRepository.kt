package com.michaelrayven.tetris_jetpack.feature_game.domain.repository

import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.GameAction
import com.michaelrayven.tetris_jetpack.feature_game.domain.model.GameInfo
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.model.Score
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    suspend fun insertScore(score: Score, gameTime: Long, date: Long): Long
    fun sendGameAction(action: GameAction)
    fun getGameState(): Flow<GameInfo>
    fun startGame()
}