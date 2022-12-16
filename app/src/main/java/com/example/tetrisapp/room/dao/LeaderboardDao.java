package com.example.tetrisapp.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.tetrisapp.room.model.LeaderboardEntry;

import java.util.List;

@Dao
public interface LeaderboardDao {
    @Query("SELECT * FROM leaderboardentry")
    List<LeaderboardEntry> getAll();

    @Query("SELECT * FROM leaderboardentry WHERE score = (SELECT MAX(score) FROM leaderboardentry) LIMIT 1")
    LeaderboardEntry getBest();

    @Insert
    void insert(LeaderboardEntry... entry);

    @Delete
    void delete(LeaderboardEntry entry);
}
