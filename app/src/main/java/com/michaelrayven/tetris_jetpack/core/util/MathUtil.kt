package com.michaelrayven.tetris_jetpack.core.util

import android.graphics.Point
import android.util.Log

fun <T>rotateMatrixClockwise(matrix: Array<Array<T>>): Array<Array<T>> {
    Log.d("MATH", "Clockwise")
    Log.d("MATH", matrix.joinToString(separator = "\n") { it.joinToString { i ->
        if (i is Boolean) {
            return@joinToString if (i) "X" else "O"
        }
        return@joinToString i.toString()
    } })
    for (x in 0 until matrix.size / 2) {
        for (y in x until matrix.size - x - 1) {
            val temp = matrix[x][y]
            matrix[x][y] = matrix[matrix.size - 1 - y][x]
            matrix[matrix.size - 1 - y][x] = matrix[matrix.size - 1 - x][matrix.size - 1 - y]
            matrix[matrix.size - 1 - x][matrix.size - 1 - y] = matrix[y][matrix.size - 1 - x]
            matrix[y][matrix.size - 1 - x] = temp
        }
    }
    Log.d("MATH", matrix.joinToString(separator = "\n") { it.joinToString { i ->
        if (i is Boolean) {
            return@joinToString if (i) "X" else "O"
        }
        return@joinToString i.toString()
    } })
    return matrix
}

fun <T>rotateMatrixCounterclockwise(matrix: Array<Array<T>>): Array<Array<T>> {
    Log.d("MATH", "Counter clockwise")
    Log.d("MATH", matrix.joinToString(separator = "\n") { it.joinToString { i ->
        if (i is Boolean) {
            return@joinToString if (i) "X" else "O"
        }
        return@joinToString i.toString()
    } })
    for (x in 0 until matrix.size / 2) {
        for (y in x until matrix.size - x - 1) {
            val temp = matrix[x][y]
            matrix[x][y] = matrix[y][matrix.size - 1 - x]
            matrix[y][matrix.size - 1 - x] = matrix[matrix.size - 1 - x][matrix.size - 1 - y]
            matrix[matrix.size - 1 - x][matrix.size - 1 - y] = matrix[matrix.size - 1 - y][x]
            matrix[matrix.size - 1 - y][x] = temp
        }
    }
    Log.d("MATH", matrix.joinToString(separator = "\n") { it.joinToString { i ->
        if (i is Boolean) {
            return@joinToString if (i) "X" else "O"
        }
        return@joinToString i.toString()
    } })
    return matrix
}

fun <T>floodFill(
    matrix: Array<Array<T>>,
    x: Int,
    y: Int,
    replacementValue: T,
    include: (x: Int, y: Int, item: T) -> Boolean
): List<Point> {
    val points = mutableListOf<Point>()
    val cols = matrix.size
    val rows = matrix[0].size

    // Condition for checking out of bounds
    if (x < 0 || x >= cols || y < 0 || y >= rows) return points
    if (!include(x, y, matrix[x][y])) return points
    matrix[x][y] = replacementValue
    points.add(Point(x, y))
    points.addAll(floodFill(
        matrix, x - 1, y, replacementValue, include
    )) // left
    points.addAll(floodFill(
        matrix, x + 1, y, replacementValue, include
    )) // right
    points.addAll(floodFill(
        matrix, x, y + 1, replacementValue, include
    )) // top
    points.addAll(floodFill(
        matrix, x, y - 1, replacementValue, include
    )) // bottom
    return points
}