package com.michaelrayven.tetris_jetpack.feature_game.data.local.entity

data class TetrominoSetEntity(
    val tetrominoes: Array<TetrominoSetEntryEntity>,
    val initialHistory: Array<String>,
    val possibleFirstTetrominoes: Array<String>,
    val shadow: TetrominoSetEntryEntity
)
