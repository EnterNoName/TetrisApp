package com.michaelrayven.tetris_jetpack.feature_game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GameStateEntity(
    val tetromino: TetrominoEntity,
    val heldTetromino: String,
    val playfield: Array<Array<String?>>,
    val sequenceGenerator: SequenceGeneratorEntity,
    val score: Int,
    val lines: Int,
    val level: Int,
    val combo: Int,
    @PrimaryKey val id: Int? = null
)
