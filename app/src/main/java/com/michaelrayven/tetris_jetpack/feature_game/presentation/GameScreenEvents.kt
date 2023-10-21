package com.michaelrayven.tetris_jetpack.feature_game.presentation

sealed class GameScreenEvents {
    object MoveLeft: GameScreenEvents()
    object MoveRight: GameScreenEvents()
    object ToggleSoftDrop: GameScreenEvents()
    object HardDrop: GameScreenEvents()
    object TogglePause: GameScreenEvents()
    data class ExitGame(val shouldSafe: Boolean): GameScreenEvents()
}
