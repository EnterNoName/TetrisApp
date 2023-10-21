package com.michaelrayven.tetris_jetpack.feature_game.di

import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.Engine
import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.Playfield
import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.tetromino_sets.DefaultTetrominoSet
import com.michaelrayven.tetris_jetpack.feature_game.data.repository.GameRepositoryImpl
import com.michaelrayven.tetris_jetpack.feature_game.domain.repository.GameRepository
import com.michaelrayven.tetris_jetpack.feature_game.domain.use_case.GameUseCases
import com.michaelrayven.tetris_jetpack.feature_game_over.data.local.ScoreDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object GameModule {
    @Provides
    fun provideGamePlayfield(): Playfield {
        return Playfield()
    }

    @Provides
    fun provideGameEngine(playfield: Playfield): Engine {
        return Engine(playfield, DefaultTetrominoSet())
    }

    @Provides
    fun provideGameRepository(engine: Engine, db: ScoreDatabase): GameRepository {
        return GameRepositoryImpl(engine, db.scoreDao)
    }

    @Provides
    fun provideUseCases(repository: GameRepository): GameUseCases {
        return GameUseCases(repository)
    }
}