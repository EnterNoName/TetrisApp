package com.example.tetrisapp.interfaces;

import com.example.tetrisapp.model.local.model.PlayerLostData;

public interface PlayerLostCallback {
    void call(PlayerLostData data);
}
