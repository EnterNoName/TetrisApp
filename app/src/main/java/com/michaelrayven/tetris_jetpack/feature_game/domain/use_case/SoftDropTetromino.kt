package com.michaelrayven.tetris_jetpack.feature_game.domain.use_case

import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.GameAction
import com.michaelrayven.tetris_jetpack.feature_game.domain.repository.GameRepository

class SoftDropTetromino(
    private val repository: GameRepository
) {
    operator fun invoke() {
        repository.sendGameAction(GameAction.SoftDropDown)
    }
}