package com.michaelrayven.tetris_jetpack.feature_game_over.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.model.Score

@Entity(indices = [Index(value = ["score"], unique = true)])
data class ScoreEntity(
    val score: Int,
    val lines: Int,
    val level: Int,
    val date: Long,
    val gameTime: Long,
    @PrimaryKey val id: Long? = null
) {
    fun toScore(): Score {
        return Score(
            score = score,
            lines = lines,
            level = level
        )
    }
}
