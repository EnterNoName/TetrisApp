package com.example.tetrisapp.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.tetrisapp.room.dao.LeaderboardDao;
import com.example.tetrisapp.room.model.LeaderboardEntry;

@Database(entities = {LeaderboardEntry.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LeaderboardDao leaderboardDao();
}
