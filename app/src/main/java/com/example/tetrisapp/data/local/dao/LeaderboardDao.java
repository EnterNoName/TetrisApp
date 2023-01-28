package com.example.tetrisapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.tetrisapp.model.local.entity.LeaderboardEntry;

import java.util.List;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface LeaderboardDao {
    @Query("SELECT * FROM leaderboardentry")
    Maybe<List<LeaderboardEntry>> getAll();

    @Query("SELECT * FROM leaderboardentry WHERE score = (SELECT MAX(score) FROM leaderboardentry) LIMIT 1")
    Single<LeaderboardEntry> getBest();

    @Query("SELECT * FROM leaderboardentry ORDER BY score DESC")
    Single<List<LeaderboardEntry>> getSorted();

    @Query("SELECT COUNT(*) FROM leaderboardentry")
    Single<Integer> getGamesCount();

    @Query("SELECT AVG(score) FROM leaderboardentry")
    Single<Integer> getAverageScore();

    @Query("SELECT AVG(level) FROM leaderboardentry")
    Single<Integer> getAverageLevel();

    @Query("SELECT AVG(lines) FROM leaderboardentry")
    Single<Integer> getAverageLines();

    @Query("SELECT MAX(score) FROM leaderboardentry")
    Single<Integer> getBestScore();

    @Query("SELECT MAX(level) FROM leaderboardentry")
    Single<Integer> getBestLevel();

    @Query("SELECT MAX(lines) FROM leaderboardentry")
    Single<Integer> getBestLines();

    @Insert
    Single<Long> insert(LeaderboardEntry leaderboardEntry);

    @Delete
    Single<Integer> delete(LeaderboardEntry entry);
}
