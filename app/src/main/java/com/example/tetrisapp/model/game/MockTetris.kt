package com.example.tetrisapp.model.game

import com.example.tetrisapp.interfaces.PlayfieldInterface
import com.example.tetrisapp.interfaces.TetrisInterface
import com.example.tetrisapp.model.game.configuration.PieceConfigurations
import java.util.*

class MockTetris : TetrisInterface {
    override var currentPiece: Piece? = null
    override var shadow: Piece? = null
    override var heldPiece: String? = null
    override var configuration = PieceConfigurations.DEFAULT.configuration
    override var playfield: PlayfieldInterface = MockPlayfield()
    override var tetrominoSequence: LinkedList<String> = LinkedList<String>()
    override var score = 0
    override var lines = 0
    override var level = 0
    override var combo = 0
    override var isLocking = false
    override val delay: Long = 0
}