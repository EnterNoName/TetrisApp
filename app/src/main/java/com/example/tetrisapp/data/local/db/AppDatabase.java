package com.example.tetrisapp.data.local.db;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.tetrisapp.data.local.dao.LeaderboardDao;
import com.example.tetrisapp.model.local.entity.LeaderboardEntry;

@Database(
        entities = {LeaderboardEntry.class},
        version = 2
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LeaderboardDao leaderboardDao();
}
