package com.example.tetrisapp.interfaces;

import com.example.tetrisapp.model.game.Piece;
import com.example.tetrisapp.model.game.Playfield;

public interface TetrisInterface {
    Piece getShadow();
    Piece getCurrentPiece();
    Playfield getPlayfield();
    PieceConfiguration getConfiguration();
    boolean isLockDelay();
    long getDelay();
    int getScore();
    int getLines();
    int getLevel();
    int getCombo();
}
