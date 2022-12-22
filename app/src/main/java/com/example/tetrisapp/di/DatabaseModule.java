package com.example.tetrisapp.di;

import android.app.Application;

import androidx.room.Room;

import com.example.tetrisapp.data.local.dao.LeaderboardDao;
import com.example.tetrisapp.data.local.db.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    static AppDatabase provideAppDatabase(Application app) {
        return Room.databaseBuilder(app, AppDatabase.class, "db").build();
    }

    @Provides
    static LeaderboardDao provideLeaderboardDao(AppDatabase db) {
        return db.leaderboardDao();
    }
}
