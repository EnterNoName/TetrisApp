package com.example.tetrisapp.di;

import android.app.Application;

import com.example.tetrisapp.util.MediaPlayerUtil;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class GameModule {
    @Provides
    @Singleton
    static MediaPlayerUtil provideMediaHelper(Application app) {
        return new MediaPlayerUtil(app.getApplicationContext());
    }
}
