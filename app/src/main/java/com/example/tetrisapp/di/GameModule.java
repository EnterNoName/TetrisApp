package com.example.tetrisapp.di;

import android.app.Application;

import com.example.tetrisapp.data.game.TetrominoRandomizer;
import com.example.tetrisapp.util.MediaHelper;

import java.util.Random;

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
    static MediaHelper provideMediaHelper(Application app) {
        return new MediaHelper(app.getApplicationContext());
    }
}
