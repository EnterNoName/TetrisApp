package com.example.tetrisapp.interfaces

interface PlayfieldInterface {
    var state: Array<Array<String?>>
    fun isValidMove(matrix: Array<ByteArray>, rowOffset: Int, colOffset: Int): Boolean
}