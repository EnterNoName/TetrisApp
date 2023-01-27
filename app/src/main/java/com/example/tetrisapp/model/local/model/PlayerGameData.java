package com.example.tetrisapp.model.local.model;

public class PlayerGameData {
    public final int score;
    public final int lines;
    public final int level;
    public final int combo;
    public final Tetromino tetromino;
    public final Tetromino tetrominoShadow;
    public final String heldTetromino;
    public final String[] tetrominoSequence;
    public final String[][] playfield;

    public String userId;
    public boolean isPlaying = true;

    public PlayerGameData(int score, int lines, int level, int combo, Tetromino tetromino, Tetromino tetrominoShadow, String heldTetromino, String[] tetrominoSequence, String[][] playfield) {
        this.score = score;
        this.lines = lines;
        this.level = level;
        this.combo = combo;
        this.tetromino = tetromino;
        this.tetrominoShadow = tetrominoShadow;
        this.heldTetromino = heldTetromino;
        this.tetrominoSequence = tetrominoSequence;
        this.playfield = playfield;
    }
}
