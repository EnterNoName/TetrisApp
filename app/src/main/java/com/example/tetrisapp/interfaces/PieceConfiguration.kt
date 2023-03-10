package com.example.tetrisapp.interfaces

import com.example.tetrisapp.model.game.Piece

interface PieceConfiguration {
    val pieces: Array<Piece>
    val names: Array<String>
    val starterPieces: Array<String>
    val initialHistory: Array<String>
    operator fun get(name: String): Piece
}