package com.example.tetris.Models;

import java.util.Random;

public class Tetromino {
    private final int[][] shapeMatrix;
    private int x;
    private int y;
    private final int color;
    public Tetromino(Shape s) {
        shapeMatrix = s.getShape();
        color = s.getColor();
        x = (int) (10 - shapeMatrix.length) / 2;
        y = 0;
    }

    public void moveUp() {
        y -= 1;
    }

    public void moveDown() {
        y += 1;
    }

    public void moveLeft() {
        x -= 1;
    }

    public void moveRight() {
        x += 1;
    }

    public void rotateCounterClockwise() {
        int N = shapeMatrix.length;
        for (int x = 0; x < N / 2; x++) {
            for (int y = x; y < N - x - 1; y++) {
                int temp = shapeMatrix[x][y];

                shapeMatrix[x][y] = shapeMatrix[y][N - x - 1];
                shapeMatrix[y][N - x - 1]
                        = shapeMatrix[N - x - 1][N - y - 1];
                shapeMatrix[N - x - 1][N - y - 1] = shapeMatrix[N - y - 1][x];
                shapeMatrix[N - y - 1][x] = temp;
            }
        }
    }

    public void rotateClockwise() {
        int N = shapeMatrix.length;
        for (int x = 0; x < N / 2; x++) {
            for (int y = x; y < N - x - 1; y++) {
                int temp = shapeMatrix[x][y];

                shapeMatrix[x][y] = shapeMatrix[N - y - 1][x];
                shapeMatrix[N - y - 1][x]
                        = shapeMatrix[N - x - 1][N - y - 1];
                shapeMatrix[N - x - 1][N - y - 1] = shapeMatrix[y][N - x - 1];
                shapeMatrix[y][N - x - 1] = temp;
            }
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    public int[][] getShapeMatrix() {
        return shapeMatrix;
    }

    public enum Shape {
        I(new int[][]{
                {0, 0, 0, 0},
                {1, 1, 1, 1},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        }, 0xff00ffff),
        O(new int[][]{
                {0, 0, 0, 0},
                {0, 1, 1, 0},
                {0, 1, 1, 0},
                {0, 0, 0, 0}
        }, 0xffffff00),
        T(new int[][]{
                {0, 0, 0},
                {1, 1, 1},
                {0, 1, 0}
        }, 0xff800080),
        S(new int[][]{
                {0, 0, 0},
                {0, 1, 1},
                {1, 1, 0}
        }, 0xff00ff00),
        Z(new int[][]{
                {0, 0, 0},
                {1, 1, 0},
                {0, 1, 1}
        }, 0xffff0000),
        J(new int[][]{
                {0, 0, 0},
                {1, 1, 1},
                {0, 0, 1}
        }, 0xff0000ff),
        L(new int[][]{
                {0, 0, 0},
                {1, 1, 1},
                {1, 0, 0}
        }, 0xffff7f00);

        private static final Shape[] VALUES = values();
        private static final int SIZE = VALUES.length;
        private final int[][] shape;
        private final int color;

        Shape(int[][] s, int c) {
            this.shape = s;
            this.color = c;
        }

        public static Shape randomShape() {
            return VALUES[new Random().nextInt(SIZE)];
        }

        public int[][] getShape() {
            return deepCopyIntMatrix(shape);
        }

        public int getColor() {
            return color;
        }
    }

    public static int[][] deepCopyIntMatrix(int[][] input) {
        if (input == null)
            return null;
        int[][] result = new int[input.length][];
        for (int r = 0; r < input.length; r++) {
            result[r] = input[r].clone();
        }
        return result;
    }
}
