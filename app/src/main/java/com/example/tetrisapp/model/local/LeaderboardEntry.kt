package com.example.tetrisapp.model.local

import androidx.room.*
import com.example.tetrisapp.data.local.converter.DateConverter
import java.util.*

@Entity(
    indices = [Index(value = ["hash"], unique = true)]
)
@TypeConverters(DateConverter::class)
class LeaderboardEntry {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "score", defaultValue = "0")
    var score = 0

    @ColumnInfo(name = "level", defaultValue = "0")
    var level = 0

    @ColumnInfo(name = "lines", defaultValue = "0")
    var lines = 0

    @ColumnInfo(name = "date")
    var date: Date? = null

    @ColumnInfo(name = "timeInGame", defaultValue = "0")
    var timeInGame = 0

    @ColumnInfo(name = "uploaded", defaultValue = "false")
    var uploaded = false

    @ColumnInfo(name = "hash")
    var hash: String? = null
}