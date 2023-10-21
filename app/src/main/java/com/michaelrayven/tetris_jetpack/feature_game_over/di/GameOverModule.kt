package com.michaelrayven.tetris_jetpack.feature_game_over.di

import android.app.Application
import androidx.room.Room
import com.michaelrayven.tetris_jetpack.feature_game_over.data.local.ScoreDatabase
import com.michaelrayven.tetris_jetpack.feature_game_over.data.repository.ScoreRepositoryImpl
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.repository.ScoreRepository
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.use_case.GameOverUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameOverModule {

    @Provides
    @Singleton
    fun provideScoreDatabase(app: Application): ScoreDatabase {
        return Room.databaseBuilder(
            app, ScoreDatabase::class.java, "score_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideScoreRepository(db: ScoreDatabase): ScoreRepository {
        return ScoreRepositoryImpl(db.scoreDao)
    }

    @Provides
    @Singleton
    fun provideUseCases(repository: ScoreRepository): GameOverUseCases {
        return GameOverUseCases(repository)
    }
}