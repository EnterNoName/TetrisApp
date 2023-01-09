package com.example.tetrisapp.model.game.configuration;

import com.example.tetrisapp.interfaces.PieceConfiguration;

public enum PieceConfigurations {
    DEFAULT(new PieceConfigurationDefault()),
    GLASS(new PieceConfigurationGlass());

    private final PieceConfiguration configuration;

    PieceConfigurations(PieceConfiguration configuration) {
        this.configuration = configuration;
    }

    public PieceConfiguration getConfiguration() {
        return this.configuration;
    }
}
