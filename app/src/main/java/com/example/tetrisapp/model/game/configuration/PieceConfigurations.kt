package com.example.tetrisapp.model.game.configuration

import com.example.tetrisapp.interfaces.PieceConfiguration

enum class PieceConfigurations(configuration: PieceConfiguration) {
    DEFAULT(PieceConfigurationDefault()),
    GLASS(PieceConfigurationGlass()),
    BRICK(PieceConfigurationBrick()),
    PORTAL_CUBE(PieceConfigurationPortalCube());

    val configuration: PieceConfiguration

    init {
        this.configuration = configuration
    }
}