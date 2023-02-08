package com.example.tetrisapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tetrisapp.model.local.entity.LeaderboardEntry;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface LeaderboardDao {
    @Query("SELECT * FROM leaderboardentry WHERE hash = :hash")
    Single<LeaderboardEntry> getByHash(String hash);

    @Query("SELECT * FROM leaderboardentry WHERE hash IS NOT NULL")
    Single<List<LeaderboardEntry>> getWithHash();

    @Query("SELECT * FROM leaderboardentry WHERE hash IS NULL")
    Single<List<LeaderboardEntry>> getWithoutHash();

    @Query("SELECT * FROM leaderboardentry WHERE id=:id")
    Single<LeaderboardEntry> getById(long id);

    @Query("SELECT * FROM leaderboardentry")
    Maybe<List<LeaderboardEntry>> getAll();

    @Query("SELECT * FROM leaderboardentry WHERE uploaded = :uploaded AND hash IS NOT NULL")
    Maybe<List<LeaderboardEntry>> getByUploaded(boolean uploaded);

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

    @Update
    Completable update(LeaderboardEntry entry);
}
