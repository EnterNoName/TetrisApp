package com.michaelrayven.tetris_jetpack.feature_game.domain.model

data class GameInfo(
    val playfield: Array<Array<String?>> = emptyArray(),
    val nextTetrominoes: Array<String> = emptyArray(),
    val heldTetromino: String? = null,
    val score: Int = 0,
    val level: Int = 0,
    val lines: Int = 0,
    val combo: Int = 0,
    val isGameOver: Boolean = false,
    val date: Long = 0L,
    val gameTime: Long = 0L
)
