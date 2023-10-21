package com.michaelrayven.tetris_jetpack.feature_game.domain.use_case

import com.michaelrayven.tetris_jetpack.feature_game.domain.repository.GameRepository

class StartGame(
    private val repository: GameRepository
) {
    operator fun invoke() {
        repository.startGame()
    }
}