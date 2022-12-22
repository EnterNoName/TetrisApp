package com.example.tetrisapp.di;

import com.example.tetrisapp.data.game.TetrominoRandomizer;

import java.util.Random;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class GameModule {
}
