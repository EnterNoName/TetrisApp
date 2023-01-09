package com.example.tetrisapp.model.remote.request;

import com.google.gson.annotations.SerializedName;

public class InvalidatePlayerDataPayload extends TokenPayload {
    @SerializedName("score")
    public int score;

    @SerializedName("lines")
    public int lines;

    @SerializedName("level")
    public int level;

    @SerializedName("tetromino")
    public Tetromino tetromino;

    @SerializedName("playfield")
    public String[][] playfield;

    public InvalidatePlayerDataPayload(String idToken, int score, int lines, int level, Tetromino tetromino, String[][] playfield) {
        super(idToken);
        this.score = score;
        this.lines = lines;
        this.level = level;
        this.tetromino = tetromino;
        this.playfield = playfield;
    }

    public class Tetromino {
        @SerializedName("name")
        public String name;

        @SerializedName("matrix")
        public byte[][] matrix;

        @SerializedName("x")
        public int x;

        @SerializedName("y")
        public int y;

        public Tetromino(String name, byte[][] matrix, int x, int y) {
            this.name = name;
            this.matrix = matrix;
            this.x = x;
            this.y = y;
        }
    }
}
