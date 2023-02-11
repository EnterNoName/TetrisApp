package com.example.tetrisapp.model.game.configuration;

import com.example.tetrisapp.interfaces.PieceConfiguration;

public enum PieceConfigurations {
    DEFAULT(new PieceConfigurationDefault()),
    GLASS(new PieceConfigurationGlass()),
    BRICK(new PieceConfigurationBrick()),
    PORTAL_CUBE(new PieceConfigurationPortalCube());

    private final PieceConfiguration configuration;

    PieceConfigurations(PieceConfiguration configuration) {
        this.configuration = configuration;
    }

    public PieceConfiguration getConfiguration() {
        return this.configuration;
    }
}
