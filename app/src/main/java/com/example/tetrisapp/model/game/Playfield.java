package com.example.tetrisapp.model.game;

public class Playfield {
    private final String[][] state = new String[22][10];

    public Playfield() {
    }

    public String[][] getState() {
        return state;
    }

    public boolean isValidMove(byte[][] matrix, int rowOffset, int colOffset) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                if (matrix[row][col] == 1 && (
                        colOffset + col < 0 ||
                                colOffset + col >= state[0].length ||
                                rowOffset + row >= state.length ||
                                state[rowOffset + row][colOffset + col] != null)
                ) {
                    return false;
                }
            }
        }

        return true;
    }
}

