package com.example.tetrisapp.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.tetrisapp.model.game.Tetris;
import com.example.tetrisapp.model.game.configuration.PieceConfiguration;
import com.example.tetrisapp.model.game.configuration.PieceConfigurationImpl;

public class GameViewModel extends ViewModel {
    private final PieceConfiguration configuration = new PieceConfigurationImpl();
    private Tetris game = new Tetris(configuration, new String[]{"I", "J", "L", "T"}, new String[]{"Z", "S", "Z", "S"});

    public Tetris getGame() {
        return game;
    }

    public PieceConfiguration getConfiguration() {
        return configuration;
    }
}
