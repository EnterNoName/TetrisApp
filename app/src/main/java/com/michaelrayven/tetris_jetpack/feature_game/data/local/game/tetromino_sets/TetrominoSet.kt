package com.michaelrayven.tetris_jetpack.feature_game.data.local.game.tetromino_sets

import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.Tetromino

interface TetrominoSet {
    val tetrominoes: Array<Tetromino>
    val names: Array<String>
    val possibleFirstTetrominoes: Array<String>
    val initialHistory: Array<String>
    val shadowName: String
    val shadowColor: Int
    operator fun get(name: String): Tetromino?
}