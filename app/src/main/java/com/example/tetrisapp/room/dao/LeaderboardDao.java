package com.example.tetrisapp.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.tetrisapp.room.model.LeaderboardEntry;

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

    @Insert
    Single<Long> insert(LeaderboardEntry leaderboardEntry);

    @Delete
    Single<Integer> delete(LeaderboardEntry entry);
}
