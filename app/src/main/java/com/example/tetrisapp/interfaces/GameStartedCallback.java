package com.example.tetrisapp.interfaces;

import com.example.tetrisapp.model.local.model.GameStartedData;

public interface GameStartedCallback {
    void call(GameStartedData data);
}
