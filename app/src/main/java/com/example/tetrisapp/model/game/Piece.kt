package com.example.tetrisapp.model.game

class Piece {
    var row = 0
    var col = 0
    var matrix: Array<ByteArray>
    val name: String
    val color: Int
    val overlayResId: Int

    constructor(
        row: Int,
        col: Int,
        name: String,
        matrix: Array<ByteArray>,
        color: Int,
        overlayResId: Int
    ) {
        this.row = row
        this.col = col
        this.name = name
        this.matrix = matrix
        this.color = color
        this.overlayResId = overlayResId
    }

    constructor(name: String, matrix: Array<ByteArray>, color: Int, overlayResId: Int) {
        this.name = name
        this.matrix = matrix
        this.color = color
        this.overlayResId = overlayResId
    }

    fun copy(): Piece {
        return Piece(
            row,
            col,
            name,
            matrix.map { it.copyOf() }.toTypedArray(),
            color,
            overlayResId
        )
    }

    override fun toString(): String {
        return name
    }
}