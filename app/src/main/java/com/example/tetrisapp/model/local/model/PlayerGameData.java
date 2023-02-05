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

    private String userId = null;
    private Boolean isPlaying = null;
    private Integer placement = null;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        if (this.userId != null) return;
        this.userId = userId;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getPlacement() {
        return placement;
    }

    public void setPlacement(int placement) {
        if (this.placement != null) return;
        this.placement = placement;
    }
}
