package com.example.tetrisapp.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.tetrisapp.model.game.MockPlayfield;
import com.example.tetrisapp.model.game.MockTetris;
import com.example.tetrisapp.model.game.Piece;
import com.example.tetrisapp.model.game.Tetris;
import com.example.tetrisapp.interfaces.PieceConfiguration;
import com.example.tetrisapp.model.game.configuration.PieceConfigurations;
import com.example.tetrisapp.model.local.model.PlayerGameData;
import com.example.tetrisapp.model.local.model.Tetromino;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public String updateMockTetris(String currentPlayerUid) {
        List<PlayerGameData> userGameValues = new ArrayList<>(userGameDataMap.values());
        userGameValues.sort(Comparator.comparingInt(o -> o.score));
        userGameValues = userGameValues.stream().filter(PlayerGameData::isPlaying).collect(Collectors.toList());

        PlayerGameData bestScoringPlayer = !userGameValues.isEmpty() ? userGameValues.get(userGameValues.size() - 1) : null;
        if (bestScoringPlayer != null && bestScoringPlayer.getUserId().equals(currentPlayerUid)) {
            bestScoringPlayer = userGameValues.size() >= 2 ? userGameValues.get(userGameValues.size() - 2) : null;
        }

        if (bestScoringPlayer == null) return null;

        // Init current piece
        Piece piece = mockTetris.getConfiguration().get(bestScoringPlayer.tetromino.name).copy();
        piece.setMatrix(bestScoringPlayer.tetromino.matrix);
        piece.setCol(bestScoringPlayer.tetromino.x);
        piece.setRow(bestScoringPlayer.tetromino.y);

        // Init current piece shadow
        Piece pieceShadow = mockTetris.getConfiguration().get(bestScoringPlayer.tetrominoShadow.name).copy();
        pieceShadow.setMatrix(bestScoringPlayer.tetrominoShadow.matrix);
        pieceShadow.setCol(bestScoringPlayer.tetrominoShadow.x);
        pieceShadow.setRow(bestScoringPlayer.tetrominoShadow.y);

        // Set opponent related game views
        mockTetris.setScore(bestScoringPlayer.score);
        mockTetris.setLevel(bestScoringPlayer.level);
        mockTetris.setLines(bestScoringPlayer.lines);
        mockTetris.setTetromino(piece);
        mockTetris.setShadow(pieceShadow);
        mockPlayfield.setState(bestScoringPlayer.playfield);

        return bestScoringPlayer.getUserId();
    }

    public String updateSpectatorMockTetris() {
        List<PlayerGameData> userGameValues = new ArrayList<>(userGameDataMap.values());
        userGameValues.sort(Comparator.comparingInt(o -> o.score));
        userGameValues = userGameValues.stream().filter(PlayerGameData::isPlaying).collect(Collectors.toList());

        PlayerGameData bestScoringPlayer = !userGameValues.isEmpty() ? userGameValues.get(userGameValues.size() - 1) : null;
        if (bestScoringPlayer == null) return null;

        // Init current piece
        Piece piece = mockTetrisSpectate.getConfiguration().get(bestScoringPlayer.tetromino.name).copy();
        piece.setMatrix(bestScoringPlayer.tetromino.matrix);
        piece.setCol(bestScoringPlayer.tetromino.x);
        piece.setRow(bestScoringPlayer.tetromino.y);

        // Init current piece shadow
        Piece pieceShadow = mockTetrisSpectate.getConfiguration().get(bestScoringPlayer.tetrominoShadow.name).copy();
        pieceShadow.setMatrix(bestScoringPlayer.tetrominoShadow.matrix);
        pieceShadow.setCol(bestScoringPlayer.tetrominoShadow.x);
        pieceShadow.setRow(bestScoringPlayer.tetrominoShadow.y);

        // Set opponent related game views
        mockTetrisSpectate.setScore(bestScoringPlayer.score);
        mockTetrisSpectate.setLevel(bestScoringPlayer.level);
        mockTetrisSpectate.setLines(bestScoringPlayer.lines);
        mockTetrisSpectate.setShadow(pieceShadow);
        mockTetrisSpectate.setTetromino(piece);
        mockPlayfieldSpectate.setState(bestScoringPlayer.playfield);

        return bestScoringPlayer.getUserId();
    }

    public int getPlacement() {
        List<PlayerGameData> userGameValues = new ArrayList<>(userGameDataMap.values());
        userGameValues.sort(Comparator.comparingInt(o -> o.score));

        int placement = userGameValues.size() + 1;
        for (int i = 0; i < userGameValues.size(); i++) {
            if (game.getScore() >= userGameValues.get(i).score) {
                placement = userGameValues.size() - i;
            }
        }

        return placement;
    }

    public String getWinnerUid() {
        List<PlayerGameData> userGameValues = new ArrayList<>(userGameDataMap.values());
        userGameValues.sort(Comparator.comparingInt(o -> o.score));

        if (userGameValues.get(userGameValues.size() - 1).score > game.getScore()) {
            return userGameValues.get(userGameValues.size() - 1).getUserId();
        } else {
            return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        }
    }

    public PlayerGameData getGameData() {
        return new PlayerGameData(
                game.getScore(),
                game.getLines(),
                game.getLevel(),
                game.getCombo(),
                new Tetromino(
                        game.getCurrentPiece().getName(),
                        game.getCurrentPiece().getMatrix(),
                        game.getCurrentPiece().getCol(),
                        game.getCurrentPiece().getRow()
                ),
                new Tetromino(
                        game.getShadow().getName(),
                        game.getShadow().getMatrix(),
                        game.getShadow().getCol(),
                        game.getShadow().getRow()
                ),
                game.getHeldPiece(),
                game.getTetrominoSequence().toArray(new String[0]),
                game.getPlayfield().getState()
        );
    }

    // Getters

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
