package com.example.tetrisapp.model.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.tetrisapp.data.local.converter.DateConverter;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity
@TypeConverters({DateConverter.class})
public class LeaderboardEntry {
    @PrimaryKey(autoGenerate = true)
    public long id = 0;

    @ColumnInfo(name = "score")
    public int score;

    @ColumnInfo(name = "level")
    public int level;

    @ColumnInfo(name = "lines")
    public int lines;

    @ColumnInfo(name = "date")
    public Date date;

    @ColumnInfo(name = "uploaded")
    public boolean uploaded = false;

    @ColumnInfo(name = "hash", index = true)
    public String hash;
}
