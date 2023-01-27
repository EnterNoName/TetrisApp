package com.example.tetrisapp.model.local.model;

public class PlayerLostData {
    public final String userId;
    public final int place;

    public PlayerLostData(String userId, int placement) {
        this.userId = userId;
        this.place = placement;
    }
}
