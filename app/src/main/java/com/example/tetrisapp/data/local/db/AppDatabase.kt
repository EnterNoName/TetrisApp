package com.example.tetrisapp.data.local.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tetrisapp.data.local.dao.LeaderboardDao
import com.example.tetrisapp.model.local.LeaderboardEntry

@Database(entities = [LeaderboardEntry::class], version = 3, autoMigrations = [
    AutoMigration (from = 2, to = 3)
])
abstract class AppDatabase : RoomDatabase() {
    abstract fun leaderboardDao(): LeaderboardDao
}