package com.michaelrayven.tetris_jetpack.feature_game.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

//@Database(
//    entities = [],
//    version = 1
//)
abstract class GameDatabase: RoomDatabase() {

    abstract val dao: GameDao
}