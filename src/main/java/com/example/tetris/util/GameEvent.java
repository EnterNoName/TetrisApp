package com.example.tetris.util;

import com.example.tetris.view.Tetris;
import com.example.tetris.model.Tetromino;

public abstract class GameEvent {
    public abstract GameEventType getType();

    public static class ScoreUpdate extends GameEvent {
        private final int score;

        public ScoreUpdate(int score) {
            this.score = score;
        }

        public int getPayload() {
            return score;
        }

        @Override
        public GameEventType getType() {
            return GameEventType.SCORE_UPDATE;
        }
    }

    public static class LinesUpdate extends GameEvent {
        private final int lines;

        public LinesUpdate(int lines) {
            this.lines = lines;
        }

        public int getPayload() {
            return lines;
        }

        @Override
        public GameEventType getType() {
            return GameEventType.LINES_UPDATE;
        }
    }

    public static class LevelUpdate extends GameEvent {
        private final int level;

        public LevelUpdate(int level) {
            this.level = level;
        }

        public int getPayload() {
            return level;
        }

        @Override
        public GameEventType getType() {
            return GameEventType.LEVEL_UPDATE;
        }
    }

    public static class NewNextPiece extends GameEvent {
        private final Tetromino.Shape nextShape;

        public NewNextPiece(Tetromino.Shape nextShape) {
            this.nextShape = nextShape;
        }

        public Tetromino.Shape getPayload() {
            return nextShape;
        }

        @Override
        public GameEventType getType() {
            return GameEventType.NEXT_PIECE;
        }
    }

    public static class GameOver extends GameEvent {
        private final Tetris.GameStatistics statistics;

        public GameOver(Tetris.GameStatistics statistics) {
            this.statistics = statistics;
        }

        public Tetris.GameStatistics getPayload() {
            return statistics;
        }

        @Override
        public GameEventType getType() {
            return GameEventType.GAME_OVER;
        }
    }

    public static class GamePause extends GameEvent {
        private final Tetris.GameStatistics statistics;

        public GamePause(Tetris.GameStatistics statistics) {
            this.statistics = statistics;
        }

        public Tetris.GameStatistics getPayload() {
            return statistics;
        }

        @Override
        public GameEventType getType() {
            return GameEventType.GAME_PAUSE;
        }
    }
}
