package com.example.tetrisapp.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.tetrisapp.model.game.MockPlayfield;
import com.example.tetrisapp.model.game.MockTetris;
import com.example.tetrisapp.model.game.Tetris;
import com.example.tetrisapp.interfaces.PieceConfiguration;
import com.example.tetrisapp.model.game.configuration.PieceConfigurationDefault;
import com.example.tetrisapp.model.game.configuration.PieceConfigurations;
import com.example.tetrisapp.model.local.model.UserGameData;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

public class GameViewModel extends ViewModel {
    private final PieceConfiguration configuration = PieceConfigurations.DEFAULT.getConfiguration();
    private final Tetris game = new Tetris(configuration, configuration.getStarterPieces(), configuration.getInitialHistory());

    private String idToken = null;
    private final MockTetris mockTetris;
    private final MockPlayfield mockPlayfield = new MockPlayfield();
    private final Map<String, UserGameData> userGameDataMap = new HashMap<>();

    public Map<String, UserGameData> getUserGameDataMap() {
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

    public GameViewModel() {
        FirebaseTokenUtil.getFirebaseToken(token -> idToken = token);

        mockTetris = new MockTetris();
        mockTetris.setConfiguration(PieceConfigurations.DEFAULT.getConfiguration());
        mockTetris.setPlayfield(mockPlayfield);
    }
}
