package com.example.tetrisapp.model.game;

import com.example.tetrisapp.interfaces.PlayfieldInterface;
import com.example.tetrisapp.interfaces.TetrisInterface;
import com.example.tetrisapp.interfaces.PieceConfiguration;

import java.util.LinkedList;

public class MockTetris implements TetrisInterface {
    private Piece tetromino;
    private Piece shadow;
    private PieceConfiguration configuration;
    private MockPlayfield playfield = new MockPlayfield();
    private LinkedList<String> tetrominoSequence = new LinkedList<>();

    private int score = 0;
    private int lines = 0;
    private int level = 0;
    private int combo = 0;

    private boolean lockDelay = false;
    private int delayLeft = 0;

    @Override
    public Piece getShadow() {
        return shadow;
    }

    @Override
    public Piece getCurrentPiece() {
        return tetromino;
    }

    @Override
    public PlayfieldInterface getPlayfield() {
        return playfield;
    }

    @Override
    public PieceConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public LinkedList<String> getTetrominoSequence() {
        return tetrominoSequence;
    }

    public void setTetrominoSequence(LinkedList<String> tetrominoSequence) {
        this.tetrominoSequence = tetrominoSequence;
    }

    @Override
    public boolean isLockDelay() {
        return lockDelay;
    }

    @Override
    public long getDelay() {
        return delayLeft;
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public int getLines() {
        return lines;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getCombo() {
        return combo;
    }

    public void setTetromino(Piece tetromino) {
        this.tetromino = tetromino;
    }

    public void setShadow(Piece shadow) {
        this.shadow = shadow;
    }

    public void setConfiguration(PieceConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setPlayfield(MockPlayfield playfield) {
        this.playfield = playfield;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCombo(int combo) {
        this.combo = combo;
    }

    public void setLockDelay(boolean lockDelay) {
        this.lockDelay = lockDelay;
    }

    public void setDelayLeft(int delayLeft) {
        this.delayLeft = delayLeft;
    }
}
