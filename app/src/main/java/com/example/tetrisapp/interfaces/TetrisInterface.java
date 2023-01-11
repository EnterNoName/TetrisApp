package com.example.tetrisapp.interfaces;

import com.example.tetrisapp.model.game.Piece;
import com.example.tetrisapp.model.game.Playfield;

import java.util.LinkedList;

public interface TetrisInterface {
    Piece getShadow();
    Piece getCurrentPiece();
    String getHeldPiece();
    PlayfieldInterface getPlayfield();
    PieceConfiguration getConfiguration();
    LinkedList<String> getTetrominoSequence();
    boolean isLockDelay();
    long getDelay();
    int getScore();
    int getLines();
    int getLevel();
    int getCombo();
}
