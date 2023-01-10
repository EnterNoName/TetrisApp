package com.example.tetrisapp.model.game;

import com.example.tetrisapp.interfaces.PlayfieldInterface;

public class MockPlayfield implements PlayfieldInterface {
    private String[][] state = new String[22][10];

    @Override
    public String[][] getState() {
        return state;
    }

    @Override
    public void setState(String[][] state) {
        this.state = state;
    }
}
