package com.michaelrayven.tetris_jetpack.feature_game.data.local.entity

data class TetrominoSetEntryEntity(
    val name: String,
    val shapeMatrix: Array<Array<Boolean>>,
    val color: Int,
    val overlay: String
)
