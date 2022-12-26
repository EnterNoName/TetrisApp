package com.example.tetrisapp.model.remote;

import com.google.gson.annotations.SerializedName;

public class ChangePlayfieldPayload extends TokenPayload {
    @SerializedName("transformation")
    public String[][] playfieldMatrix;

    public ChangePlayfieldPayload(String idToken, String[][] playfieldMatrix) {
        super(idToken);
        this.playfieldMatrix = playfieldMatrix;
    }
}
