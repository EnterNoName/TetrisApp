package com.michaelrayven.tetris_jetpack.feature_game.domain.use_case

import com.michaelrayven.tetris_jetpack.feature_game.domain.model.GameInfo
import com.michaelrayven.tetris_jetpack.feature_game.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

class GetGameState (
    private val repository: GameRepository
) {
    operator fun invoke(): Flow<GameInfo> {
        return repository.getGameState()
    }
}