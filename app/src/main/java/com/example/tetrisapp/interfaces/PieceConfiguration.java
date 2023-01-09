package com.example.tetrisapp.interfaces;

import com.example.tetrisapp.model.game.Piece;

public interface PieceConfiguration {
    Piece[] getPieces();

    String[] getNames();

    Piece get(String name);

    String[] getStarterPieces();

    String[] getInitialHistory();
}
