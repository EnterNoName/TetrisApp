package com.michaelrayven.tetris_jetpack.feature_game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SequenceGeneratorEntity(
    val pool: Array<String>,
    val history: Array<String>,
    val order: Array<String>,
    val isFirst: Boolean,
    @PrimaryKey val id: Int? = null
)
