package com.example.tetrisapp.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.tetrisapp.model.game.MockPlayfield;
import com.example.tetrisapp.model.game.MockTetris;
import com.example.tetrisapp.model.game.Tetris;
import com.example.tetrisapp.interfaces.PieceConfiguration;
import com.example.tetrisapp.model.game.configuration.PieceConfigurations;
import com.example.tetrisapp.model.local.model.PlayerGameData;
import com.example.tetrisapp.util.FirebaseTokenUtil;

import java.util.HashMap;
import java.util.Map;

public class GameViewModel extends ViewModel {
    private String idToken = null;

    private final PieceConfiguration configuration = PieceConfigurations.DEFAULT.getConfiguration();
    private final Tetris game = new Tetris(configuration, configuration.getStarterPieces(), configuration.getInitialHistory());

    private final Map<String, PlayerGameData> userGameDataMap = new HashMap<>();

    private final MockTetris mockTetris;
    private final MockPlayfield mockPlayfield = new MockPlayfield();

    private final MockTetris mockTetrisSpectate;
    private final MockPlayfield mockPlayfieldSpectate = new MockPlayfield();

    public GameViewModel() {
        FirebaseTokenUtil.getFirebaseToken(token -> idToken = token);

        mockTetris = new MockTetris();
        mockTetris.setConfiguration(PieceConfigurations.DEFAULT.getConfiguration());
        mockTetris.setPlayfield(mockPlayfield);

        mockTetrisSpectate = new MockTetris();
        mockTetrisSpectate.setConfiguration(PieceConfigurations.DEFAULT.getConfiguration());
        mockTetrisSpectate.setPlayfield(mockPlayfieldSpectate);
    }

    public Map<String, PlayerGameData> getUserGameDataMap() {
        return userGameDataMap;
    }

    public Tetris getGame() {
        return game;
    }

    public PieceConfiguration getConfiguration() {
        return configuration;
    }

    public String getIdToken() {
        return idToken;
    }

    public MockTetris getMockTetris() {
        return mockTetris;
    }

    public MockPlayfield getMockPlayfield() {
        return mockPlayfield;
    }

    public MockTetris getMockTetrisSpectate() {
        return mockTetrisSpectate;
    }

    public MockPlayfield getMockPlayfieldSpectate() {
        return mockPlayfieldSpectate;
    }
}
