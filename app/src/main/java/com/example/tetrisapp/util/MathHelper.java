package com.example.tetrisapp.util;

import java.util.Random;

public class MathHelper {
    private static final Random random = new Random();

    public static int getRandomInt(int min, int max) {
        return (int) (Math.floor(random.nextDouble() * (max - min + 1)) + min);
    }

    public static byte[][] rotateMatrixClockwise(byte[][] matrix) {
        int N = matrix.length - 1;
        for (int row = 0; row < matrix.length / 2; row++) {
            for (int col = row; col < N - row; col++) {
                byte temp = matrix[row][col];
                matrix[row][col] = matrix[N - col][row];
                matrix[N - col][row] = matrix[N - row][N - col];
                matrix[N - row][N - col] = matrix[col][N - row];
                matrix[col][N - row] = temp;
            }
        }
        return matrix;
    }

    public static byte[][] rotateMatrixCounterclockwise(byte[][] matrix) {
        int N = matrix.length - 1;
        for (int row = 0; row < matrix.length / 2; row++) {
            for (int col = row; col < N - row; col++) {
                byte temp = matrix[row][col];
                matrix[row][col] = matrix[col][N - row];
                matrix[col][N - row] = matrix[N - row][N - col];
                matrix[N - row][N - col] = matrix[N - col][row];
                matrix[N - col][row] = temp;
            }
        }
        return matrix;
    }

    public static <T> void floodFill(T[][] matrix, int i, int j, ShouldFillCallback<T> shouldFillCallback, FillCallback<T> fillCallback) {
        if (
                i < 0 || j < 0 || i >= matrix.length || j >= matrix[i].length ||
                        !shouldFillCallback.call(matrix[i][j])
        ) return;

        matrix[i][j] = fillCallback.call(matrix, i, j);

        floodFill(matrix, i + 1, j, shouldFillCallback, fillCallback);
        floodFill(matrix, i - 1, j, shouldFillCallback, fillCallback);
        floodFill(matrix, i, j + 1, shouldFillCallback, fillCallback);
        floodFill(matrix, i, j - 1, shouldFillCallback, fillCallback);
    }

    public interface ShouldFillCallback<T> {
        boolean call(T item);
    }

    public interface FillCallback<T> {
        T call(T[][] matrix, int i, int j);
    }
}
