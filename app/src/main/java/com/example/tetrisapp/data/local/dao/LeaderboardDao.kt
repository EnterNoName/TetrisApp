package com.example.tetrisapp.data.local.dao

import androidx.room.*
import com.example.tetrisapp.model.local.LeaderboardEntry

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboardentry WHERE hash = :hash")
    fun getByHash(hash: String?): LeaderboardEntry?

    @Query("SELECT * FROM leaderboardentry WHERE hash IS NOT NULL")
    fun getWithHash(): List<LeaderboardEntry>?

    @Query("SELECT * FROM leaderboardentry WHERE hash IS NULL")
    fun getWithoutHash(): List<LeaderboardEntry>?

    @Query("SELECT * FROM leaderboardentry WHERE id=:id")
    fun getById(id: Long): LeaderboardEntry?

    @Query("SELECT * FROM leaderboardentry")
    fun getAll(): List<LeaderboardEntry>?

    @Query("SELECT * FROM leaderboardentry WHERE uploaded = :uploaded AND hash IS NOT NULL")
    fun getByUploaded(uploaded: Boolean): List<LeaderboardEntry>?

    @Query("SELECT * FROM leaderboardentry WHERE score = (SELECT MAX(score) FROM leaderboardentry) LIMIT 1")
    fun getBest(): LeaderboardEntry?

    @Query("SELECT * FROM leaderboardentry ORDER BY score DESC")
    fun getSorted(): List<LeaderboardEntry>?

    @Query("SELECT SUM(timeInGame) FROM leaderboardentry")
    fun getTimeInGame(): Int?

    @Query("SELECT COUNT(*) FROM leaderboardentry")
    fun getGamesCount(): Int?

    @Query("SELECT AVG(score) FROM leaderboardentry")
    fun getAverageScore(): Int?

    @Query("SELECT AVG(level) FROM leaderboardentry")
    fun getAverageLevel(): Int?

    @Query("SELECT AVG(lines) FROM leaderboardentry")
    fun getAverageLines(): Int?

    @Query("SELECT MAX(score) FROM leaderboardentry")
    fun getBestScore(): Int?

    @Query("SELECT MAX(level) FROM leaderboardentry")
    fun getBestLevel(): Int?

    @Query("SELECT MAX(lines) FROM leaderboardentry")
    fun getBestLines(): Int?

    @Insert
    fun insert(leaderboardEntry: LeaderboardEntry?): Long?

    @Delete
    fun delete(entry: LeaderboardEntry?)

    @Update
    fun update(entry: LeaderboardEntry?)
}