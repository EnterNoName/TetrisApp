package com.michaelrayven.tetris_jetpack.feature_game.data.local.game

sealed class GameAction {
    object MoveLeft: GameAction()
    object MoveRight: GameAction()
    object MoveDown: GameAction()
    object RotateClockwise: GameAction()
    object RotateCounterClockwise: GameAction()
    object SoftDropDown: GameAction()
    object HardDropDown: GameAction()
    object Hold: GameAction()
}
