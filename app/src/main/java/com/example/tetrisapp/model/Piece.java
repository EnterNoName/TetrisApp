package com.example.tetrisapp.model;

import androidx.annotation.NonNull;

import com.example.tetrisapp.util.ArrayHelper;

public class Piece {
    private int row = 0;
    private int col = 0;
    private byte[][] matrix;
    private final String name;
    private final int color;
    private final int overlayResId;

    public Piece(int row, int col, String name, byte[][] matrix, int color, int overlayResId) {
        this.row = row;
        this.col = col;
        this.name = name;
        this.matrix = matrix;
        this.color = color;
        this.overlayResId = overlayResId;
    }

    public Piece(String name, byte[][] matrix, int color, int overlayResId) {
        this.name = name;
        this.matrix = matrix;
        this.color = color;
        this.overlayResId = overlayResId;
    }

    public Piece copy() {
        return new Piece(row, col, name, ArrayHelper.deepCopy(matrix), color, overlayResId);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public byte[][] getMatrix() {
        return matrix;
    }

    public int getColor() {
        return color;
    }

    public int getOverlayResId() {
        return overlayResId;
    }

    public String getName() {
        return name;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setMatrix(byte[][] matrix) {
        this.matrix = matrix;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
