package com.example.tetrisapp.model.game.configuration;

import com.example.tetrisapp.model.game.Piece;

public interface PieceConfiguration {
    Piece[] getPieces();

    String[] getNames();

    Piece get(String name);
}
