package com.michaelrayven.tetris_jetpack.feature_game_over.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaelrayven.tetris_jetpack.feature_game_over.data.local.entity.ScoreEntity

@Dao
interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScores(scores: List<ScoreEntity>): List<Long>

    @Query("SELECT a.* " +
            "FROM scoreentity a " +
            "LEFT OUTER JOIN scoreentity b " +
            "    ON a.id = b.id AND a.score < b.score " +
            "WHERE b.id IS NULL; ")
    suspend fun getMaxScore(): ScoreEntity?

    @Query("SELECT * " +
            "FROM scoreentity " +
            "WHERE id IS :id; ")
    suspend fun getById(id: Long): ScoreEntity?

    @Query("SELECT * " +
            "FROM scoreentity " +
            "WHERE score IS :score; ")
    suspend fun getByScore(score: Int): ScoreEntity?
}