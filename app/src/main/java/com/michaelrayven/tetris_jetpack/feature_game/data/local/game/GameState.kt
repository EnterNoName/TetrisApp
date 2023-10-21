package com.michaelrayven.tetris_jetpack.feature_game.data.local.game

data class GameState(
    val playfield: Playfield = Playfield(),
    val currentTetromino: Tetromino = Tetromino(),
    val nextTetrominoes: Array<String> = emptyArray(),
    val shadowYOffset: Int = 0,
    val heldTetromino: String? = null,
    val score: Int = 0,
    val level: Int = 0,
    val lines: Int = 0,
    val combo: Int = 0,
    val isGameOver: Boolean = false,
    val date: Long = 0L,
    val gameTime: Long = 0L
) {
}