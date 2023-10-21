package com.michaelrayven.tetris_jetpack.feature_game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO: Account for tetromino set changes
@Entity
data class TetrominoEntity(
    val row: Int,
    val col: Int,
    val name: String,
    val shapeMatrix: Array<Array<Boolean>>,
    @PrimaryKey val id: Int? = null
)
