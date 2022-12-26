package com.example.tetrisapp.model.remote;

import com.google.gson.annotations.SerializedName;

public class MovePiecePayload extends TokenPayload {
    @SerializedName("name")
    public String pieceName;
    @SerializedName("transformation")
    public byte[][] pieceMatrix;
    @SerializedName("xPos")
    public int x;
    @SerializedName("yPos")
    public int y;

    public MovePiecePayload(String idToken, String pieceName, byte[][] pieceMatrix, int x, int y) {
        super(idToken);
        this.pieceName = pieceName;
        this.pieceMatrix = pieceMatrix;
        this.x = x;
        this.y = y;
    }
}
