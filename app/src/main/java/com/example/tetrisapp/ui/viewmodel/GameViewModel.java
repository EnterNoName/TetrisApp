package com.example.tetrisapp.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.tetrisapp.model.game.Tetris;
import com.example.tetrisapp.interfaces.PieceConfiguration;
import com.example.tetrisapp.model.game.configuration.PieceConfigurationDefault;
import com.example.tetrisapp.model.game.configuration.PieceConfigurations;

public class GameViewModel extends ViewModel {
    private final PieceConfiguration configuration = PieceConfigurations.GLASS.getConfiguration();
    private Tetris game = new Tetris(configuration, configuration.getStarterPieces(), configuration.getInitialHistory());

    public Tetris getGame() {
        return game;
    }

    public PieceConfiguration getConfiguration() {
        return configuration;
    }
}
