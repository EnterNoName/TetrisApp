package com.example.tetrisapp.model.local.model;

public class Tetromino {
    public final String name;
    public final byte[][] matrix;
    public final int x;
    public final int y;

    public Tetromino(String name, byte[][] matrix, int x, int y) {
        this.name = name;
        this.matrix = matrix;
        this.x = x;
        this.y = y;
    }
}
