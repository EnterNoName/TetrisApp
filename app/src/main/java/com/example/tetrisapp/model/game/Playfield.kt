package com.example.tetrisapp.model.game

import com.example.tetrisapp.interfaces.PlayfieldInterface

class Playfield : PlayfieldInterface {
    override var state = Array(22) { arrayOfNulls<String>(10) }
    override fun isValidMove(matrix: Array<ByteArray>, rowOffset: Int, colOffset: Int): Boolean {
        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                if (
                    matrix[row][col].toInt() == 1
                    && (colOffset + col < 0
                            || colOffset + col >= state[0].size
                            || rowOffset + row >= state.size
                            || state[rowOffset + row][colOffset + col] != null)
                ) {
                    return false
                }
            }
        }
        return true
    }
}