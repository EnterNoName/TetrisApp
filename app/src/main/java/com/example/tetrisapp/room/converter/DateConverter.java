package com.example.tetrisapp.room.converter;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public long fromDate(Date date) {
        return date.getTime();
    }

    @TypeConverter
    public Date toDate(long time) {
        return new Date(time);
    }
}
