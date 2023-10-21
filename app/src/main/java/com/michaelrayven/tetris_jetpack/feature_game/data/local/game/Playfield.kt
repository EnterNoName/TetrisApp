package com.michaelrayven.tetris_jetpack.feature_game.data.local.game

import android.graphics.Point
import android.util.Log
import com.michaelrayven.tetris_jetpack.core.util.deepCopy
import com.michaelrayven.tetris_jetpack.core.util.floodFill

class Playfield(
    // Visible width and height
    val width: Int = 10,
    val height: Int = 20
) {
    // Playfield matrix goes in columns than rows (supply coordinates as [x][y])
    val state: Array<Array<String?>> = Array(width) { arrayOfNulls(height) }

    fun deepStateCopy(): Array<Array<String?>> {
        return state.map { it.copyOf() }.toTypedArray()
    }

    // If doesn't succeed the game is over
    fun bindTetromino(tetromino: Tetromino): Boolean {
        for (col in tetromino.shapeMatrix.indices) {
            for (row in tetromino.shapeMatrix[col].indices) {
                if (tetromino.shapeMatrix[col][row]) {
                    if (tetromino.row + row < 0) {
                        return false
                    }
                    state[tetromino.col + col][tetromino.row + row] = tetromino.name
                }
            }
        }
        return true
    }

    private fun bindShape(shape: List<Point>, offsetY: Int) {
        if (offsetY == 0) return
        val stateCopy = state.deepCopy()
        shape.forEach {
            state[it.x][it.y] = null
        }
        shape.forEach {
            state[it.x][it.y + offsetY] = stateCopy[it.x][it.y]
        }
    }

    fun isValidMove(shapeMatrix: Array<Array<Boolean>>, x: Int, y: Int): Boolean {
        for (col in shapeMatrix.indices) {
            for (row in shapeMatrix[col].indices) {
                // If row + y is less than zero move should be valid
                // To allow tetromino to spawn above the board
                if (
                    !(row + y < 0) &&
                    shapeMatrix[col][row] &&
                    (col + x < 0
                            || col + x >= state.size
                            || row + y >= state[0].size
                            || state[col + x][row + y] != null)
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun isValidMove(points: List<Point>, offsetY: Int): Boolean {
        val stateCopy = state.deepCopy()
        points.forEach { point ->
            stateCopy[point.x][point.y] = null
        }
        points.forEach { point ->
            if (point.y + offsetY >= stateCopy[0].size ||
                stateCopy[point.x][point.y + offsetY] != null) {
                return false
            }
        }
        return true
    }

    // Returns the count of lines cleared
    fun clearLines(): Int {
        var clearedLinesTotal = 0
        var clearedLines: Int

        do {
            clearedLines = 0
            for (row in state[0].indices) {
                var isFilledLine = true
                for (col in state.indices) {
                    if (state[col][row] == null) {
                        isFilledLine = false
                        break
                    }
                }

                if (isFilledLine) {
                    for (col in state.indices) {
                        state[col][row] = null
                    }
                    clearedLines++
                }
            }
            clearedLinesTotal += clearedLines
            if (clearedLines > 0) {
                applyGravity()
            }
        } while (clearedLines > 0)

        return clearedLinesTotal
    }

    private fun applyGravity() {
        val shapes = mutableListOf<List<Point>>()

        val possiblePoints = state.mapIndexed { i, arr ->
            arr.mapIndexed { j, _ ->
                Point(i, j)
            }
        }.reduce { acc, points -> acc.plus(points) }.toMutableList()

        var i = 0
        while (i < possiblePoints.size) {
            val point = possiblePoints[i]
            if (state[point.x][point.y] != null) {
                val shape = floodFill(state.deepCopy(), point.x, point.y, null, { _, _, item -> item != null })
                shape.forEach { p ->
                    val index = possiblePoints.indexOf(p)
                    if (index < i) {
                        i -= i - index
                    }
                    possiblePoints.removeAt(index)
                }
                shapes.add(shape)
            }
            i++
        }

        shapes.sortByDescending { it.maxBy { p -> p.y }.y }

        shapes.forEach { shape ->
            var offsetY = 1
            while (offsetY < height) {
                if (!isValidMove(shape, offsetY)) {
                    offsetY--
                    break
                }
                offsetY++
            }

            bindShape(shape, offsetY)
        }
    }
 }