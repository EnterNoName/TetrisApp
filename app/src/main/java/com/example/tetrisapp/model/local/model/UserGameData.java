package com.example.tetrisapp.model.local.model;

public class UserGameData {
    public final String userId;
    public final int score;
    public final int lines;
    public final int level;
    public final Tetromino tetromino;
    public final String[][] playfield;

    public UserGameData(String userId, int score, int lines, int level, Tetromino tetromino, String[][] playfield) {
        this.userId = userId;
        this.score = score;
        this.lines = lines;
        this.level = level;
        this.tetromino = tetromino;
        this.playfield = playfield;
    }
}
