package com.example.tetrisapp.interfaces;

import com.example.tetrisapp.model.local.model.PlayerGameData;

public interface PlayerLostCallback {
    void call(PlayerGameData data);
}
