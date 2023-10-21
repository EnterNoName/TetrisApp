package com.michaelrayven.tetris_jetpack.feature_game.domain.use_case

import com.michaelrayven.tetris_jetpack.feature_game.domain.repository.GameRepository

class GameUseCases(
    private val repository: GameRepository
) {
    val StartGame = StartGame(repository)
    val PauseGame = PauseGame(repository)
    val ExitGame = ExitGame(repository)
    val GetGameState = GetGameState(repository)
    val SoftDropTetromino = SoftDropTetromino(repository)
    val HardDropTetromino = HardDropTetromino(repository)
    val MoveTetrominoLeft = MoveTetrominoLeft(repository)
    val MoveTetrominoRight = MoveTetrominoRight(repository)
    val RotateTetrominoClockwise = RotateTetrominoClockwise(repository)
    val RotateTetrominoCounterClockwise = RotateTetrominoCounterClockwise(repository)
    val HoldTetromino = HoldTetromino(repository)
    val InsertScore = InsertScore(repository)
}