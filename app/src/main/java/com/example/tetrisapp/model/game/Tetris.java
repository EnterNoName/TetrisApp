package com.example.tetrisapp.model.game;

import com.example.tetrisapp.data.game.TetrominoRandomizer;
import com.example.tetrisapp.model.game.configuration.PieceConfiguration;
import com.example.tetrisapp.util.ArrayHelper;
import com.example.tetrisapp.util.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import kotlin.Pair;

public class Tetris {
    public static final int GENERATE_AHEAD = 4;
    public static final int DEFAULT_SPEED = 750;
    public static final int LOCK_DELAY = 500;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private Callback onGameValuesUpdateCallback = () -> {};
    private Callback onMoveCallback = () -> {};
    private Callback onPauseCallback = () -> {};
    private Callback onResumeCallback = () -> {};
    private Callback onGameOverCallback = () -> {};
    private Callback onLineClearCallback = () -> {};
    private Callback onSolidifyCallback = () -> {};
    private Callback onHoldCallback = () -> {};
    private Callback onHardDropCallback = () -> {};

    private ScheduledFuture<?> future;

    // In-Game values
    private Piece tetromino;
    private Piece shadow;
    private Piece heldPiece;

    private final PieceConfiguration configuration;
    private final Playfield playfield = new Playfield();
    private final LinkedList<String> tetrominoSequence = new LinkedList<>();
    private final TetrominoRandomizer randomizer;

    private int score = 0;
    private int lines = 0;
    private int level = 0;
    private int combo = 0;
    private int speed = DEFAULT_SPEED;

    private boolean gameOver = false;
    private boolean pause = true;
    private boolean lockDelay = false;
    private boolean softDrop = false;
    private boolean holdUsed = false;
    private int delayLeft = 0;

    public Tetris(PieceConfiguration configuration, String[] starterPieces, String[] initialHistory) {
        this.randomizer = new TetrominoRandomizer(configuration.getNames(), starterPieces, initialHistory);
        this.configuration = configuration;
        tetromino = getNextTetromino();
        calculateShadow();

        onGameValuesUpdateCallback.call();
    }

    private synchronized void updateSpeed(int delay, float multiplier) {
        if (future != null) {
            future.cancel(true);
        }

        future = executor.scheduleAtFixedRate(new GameExecutor(), delay, (int) (speed * multiplier), TimeUnit.MILLISECONDS);
    }

    private void calculateShadow() {
        int yOffset = 0;
        while (playfield.isValidMove(tetromino.getMatrix(), tetromino.getRow() + yOffset + 1, tetromino.getCol())) {
            yOffset++;
        }

        shadow = tetromino.copy();
        shadow.setRow(shadow.getRow() + yOffset);
    }

    private Piece getNextTetromino() {
        if (tetrominoSequence.size() < GENERATE_AHEAD + 1) {
            generateSequence();
        }

        String name = tetrominoSequence.remove();
        Piece piece = configuration.get(name).copy();

        int col = (int) (playfield.getState()[0].length / 2 - Math.ceil(piece.getMatrix().length / 2f));
        piece.setCol(col);
        return piece;
    }

    private void generateSequence() {
        while (tetrominoSequence.size() < GENERATE_AHEAD * 2 + 1) {
            tetrominoSequence.add(randomizer.next());
        }
    }

    private void placeTetromino() {
        for (int row = 0; row < tetromino.getMatrix().length; row++) {
            for (int col = 0; col < tetromino.getMatrix()[row].length; col++) {
                if (tetromino.getMatrix()[row][col] == 1) {
                    if (tetromino.getRow() + row < 2) {
                        if (!gameOver) {
                            gameOver = true;
                            onGameOverCallback.call();
                        }
                        return;
                    }

                    playfield.getState()[tetromino.getRow() + row][tetromino.getCol() + col] = tetromino.getName();
                }
            }
        }

        tetromino = getNextTetromino();
        int linesCleared = clearLines();
        calculateShadow();

        onSolidifyCallback.call();

        updateGameValues(linesCleared);
        updateSpeed(0, 1f);
        this.softDrop = false;
        this.holdUsed = false;

        onGameValuesUpdateCallback.call();
    }

    private void updateGameValues(int linesCleared) {
        if (linesCleared == 0) {
            this.combo = 0;
            return;
        }

        this.lines += linesCleared;
        this.level = this.lines / 10;
        switch (linesCleared) {
            case 1:
                this.score += (40 + 10 * this.combo) * (this.level + 1);
                break;
            case 2:
                this.score += (100 + 10 * this.combo) * (this.level + 1);
                break;
            case 3:
                this.score += (300 + 10 * this.combo) * (this.level + 1);
                break;
            case 4:
                this.score += (1200 + 10 * this.combo) * (this.level + 1);
                break;
            default:
                this.score += (300 + 10 * this.combo) * linesCleared * (this.level + 1);
                break;
        }
        this.combo += linesCleared;
        this.speed = Math.max(DEFAULT_SPEED - this.level * 50, 50);

        onLineClearCallback.call();
    }

    private int clearLines() {
        int clearLinesTotal = 0;

        for (int row = playfield.getState().length - 1; row >= 2; row--) {
            int clearLinesCount = 0;
            if (Arrays.stream(playfield.getState()[row]).allMatch(Objects::nonNull)) {
                do {
                    playfield.getState()[row - clearLinesCount] = new String[10]; // Clear the column
                    clearLinesCount++;
                } while (Arrays.stream(playfield.getState()[row - clearLinesCount]).allMatch(Objects::nonNull));

                String[][] playfieldStateCopy = ArrayHelper.deepCopy(playfield.getState());
                ArrayList<ArrayList<Pair<Integer, Integer>>> positionsList = new ArrayList<>();

                for (int y = row - clearLinesCount; y >= 2; y--) {
                    for (int x = 0; x < playfield.getState()[y].length; x++) {
                        if (playfield.getState()[y][x] != null) {
                            ArrayList<Pair<Integer, Integer>> positions = new ArrayList<>();
                            MathHelper.floodFill(playfield.getState(), y, x, Objects::nonNull, (matrix, i, j) -> {
                                positions.add(new Pair<>(i, j));
                                return null;
                            });
                            positionsList.add(positions);
                        }
                    }
                }

                int yOffset = 1;
                while (!positionsList.isEmpty()) {
                    int finalYOffset = yOffset + clearLinesCount;
                    for (int i = 0; i < positionsList.size(); ) {
                        ArrayList<Pair<Integer, Integer>> pos = positionsList.get(i);

                        if (pos.stream().anyMatch(p -> {
                            int y = p.getFirst() + finalYOffset;
                            int x = p.getSecond();
                            return y >= playfield.getState().length || playfield.getState()[y][x] != null;
                        })) {
                            positionsList.remove(i);
                            pos.forEach(p -> {
                                int yInitial = p.getFirst();
                                int y = p.getFirst() + finalYOffset - 1;
                                int x = p.getSecond();

                                playfield.getState()[y][x] = playfieldStateCopy[yInitial][x];
                            });
                        } else {
                            i++;
                        }
                    }

                    yOffset++;
                }

                row += yOffset - 1;
            }
            clearLinesTotal += clearLinesCount;
        }

        return clearLinesTotal;
    }

    // Controls

    public void moveTetrominoRight() {
        if (!pause && playfield.isValidMove(tetromino.getMatrix(), tetromino.getRow(), tetromino.getCol() + 1)) {
            tetromino.setCol(tetromino.getCol() + 1);
            onMoveCallback.call();
            calculateShadow();
        }
    }

    public void moveTetrominoLeft() {
        if (!pause && playfield.isValidMove(tetromino.getMatrix(), tetromino.getRow(), tetromino.getCol() - 1)) {
            tetromino.setCol(tetromino.getCol() - 1);
            onMoveCallback.call();
            calculateShadow();
        }
    }

    public void rotateTetrominoRight() {
        if (pause) return;

        byte[][] rotatedMatrix = MathHelper.rotateMatrixClockwise(ArrayHelper.deepCopy(tetromino.getMatrix()));

        if (playfield.isValidMove(rotatedMatrix, tetromino.getRow(), tetromino.getCol())) {
            tetromino.setMatrix(rotatedMatrix);
            onMoveCallback.call();
            calculateShadow();
        }

    }

    public void rotateTetrominoLeft() {
        if (pause) return;

        byte[][] rotatedMatrix = MathHelper.rotateMatrixCounterclockwise(ArrayHelper.deepCopy(tetromino.getMatrix()));

        if (playfield.isValidMove(rotatedMatrix, tetromino.getRow(), tetromino.getCol())) {
            tetromino.setMatrix(rotatedMatrix);
            onMoveCallback.call();
            calculateShadow();
        }
    }

    private void moveTetrominoDown() {
        if (pause) return;

        if (playfield.isValidMove(tetromino.getMatrix(), tetromino.getRow() + 1, tetromino.getCol())) {
            tetromino.setRow(tetromino.getRow() + 1);
            onMoveCallback.call();

            lockDelay = false;
        } else {
            if (!lockDelay) {
                updateSpeed(LOCK_DELAY, 1f);
            } else {
                placeTetromino();
            }

            lockDelay = !lockDelay;
        }
    }

    public void hardDrop() {
        if (pause) return;
        onHardDropCallback.call();
        future.cancel(true); // Prevents out of bounds from hardDrop timed with movePieceDown
        while (playfield.isValidMove(tetromino.getMatrix(), tetromino.getRow() + 1, tetromino.getCol())) {
            tetromino.setRow(tetromino.getRow() + 1);
        }
        placeTetromino();
    }

    public void hold() {
        if (pause) return;

        if (!holdUsed) {
            if (heldPiece == null) {
                heldPiece = configuration.get(tetromino.getName()).copy();
                tetromino = getNextTetromino();
            } else {
                Piece temp = heldPiece.copy();
                heldPiece = configuration.get(tetromino.getName()).copy();
                tetromino = temp;
                int col = (int) (playfield.getState()[0].length / 2 - Math.ceil(tetromino.getMatrix().length / 2f));
                tetromino.setCol(col);
            }

            calculateShadow();
            onHoldCallback.call();

            holdUsed = true;
        }
    }

    // Game engine

    private class GameExecutor implements Runnable {
        @Override
        public void run() {
            if (gameOver) {
                future.cancel(true);
            } else {
                moveTetrominoDown();
            }
        }
    }

    // Getters

    public Piece getCurrentPiece() {
        return tetromino;
    }

    public Piece getHeldPiece() {
        return heldPiece;
    }

    public Playfield getPlayfield() {
        return playfield;
    }

    public LinkedList<String> getTetrominoSequence() {
        return tetrominoSequence;
    }

    public PieceConfiguration getConfiguration() {
        return configuration;
    }

    public int getScore() {
        return score;
    }

    public int getLines() {
        return lines;
    }

    public int getLevel() {
        return level;
    }

    public int getCombo() {
        return combo;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPaused() {
        return pause;
    }

    public boolean isSoftDrop() {
        return softDrop;
    }

    public boolean isHoldUsed() {
        return holdUsed;
    }

    public boolean isLockDelay() {
        return lockDelay;
    }

    public long getDelay() {
        if (future != null)
            return future.getDelay(TimeUnit.MILLISECONDS);
        return 0;
    }

    public Piece getShadow() {
        return this.shadow;
    }

    // Setters

    public void setPause(boolean pause) {
        if (!gameOver) {
            if (pause && future != null) {
                delayLeft = (int) future.getDelay(TimeUnit.MILLISECONDS);
                future.cancel(true);

                onPauseCallback.call();
            } else {
                updateSpeed(delayLeft, 1f);
                delayLeft = 0;
                onResumeCallback.call();
            }
            this.pause = pause;
        }
    }

    public void setSoftDrop(boolean softDrop) {
        if (!pause) {
            this.softDrop = softDrop;

            if (softDrop) {
                updateSpeed(0, 0.25f);
            } else {
                updateSpeed(0, 1f);
            }
        }
    }

    // Callback

    public void setOnGameValuesUpdate(Callback callback) {
        this.onGameValuesUpdateCallback = callback;
    }

    public void setOnMove(Callback callback) {
        this.onMoveCallback = callback;
    }

    public void setOnPause(Callback callback) {
        this.onPauseCallback = callback;
    }

    public void setOnResume(Callback callback) {
        this.onResumeCallback = callback;
    }

    public void setOnGameOver(Callback callback) {
        this.onGameOverCallback = callback;
    }

    public void setOnSolidify(Callback callback) {
        this.onSolidifyCallback = callback;
    }

    public void setOnLineClear(Callback callback) {
        this.onLineClearCallback = callback;
    }

    public void setOnHold(Callback callback) {
        this.onHoldCallback = callback;
    }

    public void setOnHardDrop(Callback callback) {
        this.onHardDropCallback = callback;
    }

    public interface Callback {
        void call();
    }
}