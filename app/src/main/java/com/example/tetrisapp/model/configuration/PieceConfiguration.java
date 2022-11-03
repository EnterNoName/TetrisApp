package com.example.tetrisapp.model.configuration;

import com.example.tetrisapp.model.Piece;

public interface PieceConfiguration {
    Piece[] getPieces();
    String[] getNames();
    Piece get(String name);
}
