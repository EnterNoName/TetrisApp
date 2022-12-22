package com.example.tetrisapp.data.local.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.tetrisapp.data.local.dao.LeaderboardDao;
import com.example.tetrisapp.model.local.entity.LeaderboardEntry;

@Database(entities = {LeaderboardEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LeaderboardDao leaderboardDao();
}
