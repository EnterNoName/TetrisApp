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

    public final String userId;
    public final Boolean isPlaying;
    public final Integer placement;

    public PlayerGameData(String userId, int score, int lines, int level, int combo, Tetromino tetromino, Tetromino tetrominoShadow, String heldTetromino, String[] tetrominoSequence, String[][] playfield, boolean isPlaying, Integer placement) {
        this.userId = userId;
        this.score = score;
        this.lines = lines;
        this.level = level;
        this.combo = combo;
        this.tetromino = tetromino;
        this.tetrominoShadow = tetrominoShadow;
        this.heldTetromino = heldTetromino;
        this.tetrominoSequence = tetrominoSequence;
        this.playfield = playfield;
        this.isPlaying = isPlaying;
        this.placement = placement;
    }

    public PlayerGameData(PlayerGameData data, boolean isPlaying, Integer placement) {
        this.userId = data.userId;
        this.score = data.score;
        this.lines = data.lines;
        this.level = data.level;
        this.combo = data.combo;
        this.tetromino = data.tetromino;
        this.tetrominoShadow = data.tetrominoShadow;
        this.heldTetromino = data.heldTetromino;
        this.tetrominoSequence = data.tetrominoSequence;
        this.playfield = data.playfield;
        this.isPlaying = isPlaying;
        this.placement = placement;
    }

//    public String getUserId() {
//        return userId;
//    }
//
//    public void setUserId(String userId) {
//        if (this.userId != null) return;
//        this.userId = userId;
//    }
//
//    public boolean isPlaying() {
//        return isPlaying;
//    }
//
//    public void setPlaying(boolean playing) {
//        isPlaying = playing;
//    }
//
//    public int getPlacement() {
//        return placement;
//    }
//
//    public void setPlacement(int placement) {
//        if (this.placement != null) return;
//        this.placement = placement;
//    }
}
