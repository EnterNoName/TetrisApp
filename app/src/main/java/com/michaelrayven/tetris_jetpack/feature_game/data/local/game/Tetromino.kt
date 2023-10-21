package com.michaelrayven.tetris_jetpack.feature_game.data.local.game

import androidx.compose.ui.geometry.Size
import kotlin.math.max

data class Tetromino(
    val row: Int = 0,
    val col: Int = 0,
    val name: String = "",
    val shapeMatrix: Array<Array<Boolean>> = emptyArray(),
    val color: Int = 0,
    val overlayResId: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tetromino

        if (!shapeMatrix.contentDeepEquals(other.shapeMatrix)) return false

        return true
    }

    override fun hashCode(): Int {
        return shapeMatrix.contentDeepHashCode()
    }

    override fun toString(): String {
        return this.name
    }

    fun getDimensions(): Size {
        var tetrominoWidth = 0
        var tetrominoHeight = 0

        var rowWidth = 0
        for (i in shapeMatrix.indices) {
            var colHeight = 0
            for (j in shapeMatrix[0].indices) {
                if (shapeMatrix[i][j]) {
                    colHeight++
                }
            }
            if (colHeight > 0) {
                rowWidth++
            }
            tetrominoHeight = max(tetrominoHeight, colHeight)
        }
        tetrominoWidth = rowWidth

        return Size(tetrominoWidth.toFloat(), tetrominoHeight.toFloat())
    }
}