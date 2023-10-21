package com.michaelrayven.tetris_jetpack.feature_game_over.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.michaelrayven.tetris_jetpack.feature_game_over.data.local.entity.ScoreEntity

@Database(
    entities = [ScoreEntity::class],
    version = 1
)
abstract class ScoreDatabase: RoomDatabase() {
    abstract val scoreDao: ScoreDao
}