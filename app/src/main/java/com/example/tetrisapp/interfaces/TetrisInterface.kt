package com.example.tetrisapp.interfaces

import com.example.tetrisapp.model.game.Piece
import java.util.*

interface TetrisInterface {
    var shadow: Piece?
    var currentPiece: Piece?
    var heldPiece: String?
    var playfield: PlayfieldInterface
    var configuration: PieceConfiguration
    var tetrominoSequence: LinkedList<String>
    var isLocking: Boolean
    var score: Int
    var lines: Int
    var level: Int
    var combo: Int
    val delay: Long
}