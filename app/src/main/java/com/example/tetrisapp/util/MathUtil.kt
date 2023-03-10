package com.example.tetrisapp.util

object MathUtil {
    fun rotateMatrixClockwise(matrix: Array<ByteArray>): Array<ByteArray> {
        val n = matrix.size - 1
        for (row in 0 until matrix.size / 2) {
            for (col in row until n - row) {
                val temp = matrix[row][col]
                matrix[row][col] = matrix[n - col][row]
                matrix[n - col][row] = matrix[n - row][n - col]
                matrix[n - row][n - col] = matrix[col][n - row]
                matrix[col][n - row] = temp
            }
        }
        return matrix
    }

    fun rotateMatrixCounterclockwise(matrix: Array<ByteArray>): Array<ByteArray> {
        val n = matrix.size - 1
        for (row in 0 until matrix.size / 2) {
            for (col in row until n - row) {
                val temp = matrix[row][col]
                matrix[row][col] = matrix[col][n - row]
                matrix[col][n - row] = matrix[n - row][n - col]
                matrix[n - row][n - col] = matrix[n - col][row]
                matrix[n - col][row] = temp
            }
        }
        return matrix
    }

    fun <T> floodFill(
        matrix: Array<Array<T>>,
        i: Int,
        j: Int,
        shouldFillCallback: (item: T) -> Boolean,
        fillCallback: (matrix: Array<Array<T>>, i: Int, j: Int) -> T
    ) {
        if (i < 0 || j < 0 || i >= matrix.size || j >= matrix[i].size ||
            !shouldFillCallback(matrix[i][j])
        ) return
        matrix[i][j] = fillCallback(matrix, i, j)
        floodFill(matrix, i + 1, j, shouldFillCallback, fillCallback)
        floodFill(matrix, i - 1, j, shouldFillCallback, fillCallback)
        floodFill(matrix, i, j + 1, shouldFillCallback, fillCallback)
        floodFill(matrix, i, j - 1, shouldFillCallback, fillCallback)
    }
}