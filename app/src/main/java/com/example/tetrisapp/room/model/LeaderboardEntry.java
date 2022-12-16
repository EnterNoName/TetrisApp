package com.example.tetrisapp.room.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class LeaderboardEntry {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "score")
    public int score;

    @ColumnInfo(name = "level")
    public int level;

    @ColumnInfo(name = "lines")
    public int lines;

    @ColumnInfo(name = "date")
    public Date date;
}
