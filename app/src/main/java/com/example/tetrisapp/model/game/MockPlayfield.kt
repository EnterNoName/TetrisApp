package com.example.tetrisapp.model.game

import com.example.tetrisapp.interfaces.PlayfieldInterface

class MockPlayfield : PlayfieldInterface {
    override var state = Array(22) { arrayOfNulls<String>(10) }
    override fun isValidMove(matrix: Array<ByteArray>, rowOffset: Int, colOffset: Int): Boolean {
        return true
    }
}