package com.example.tetris.Models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.tetris.R;
import com.example.tetris.Utils.OnSwipeTouchListener;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kotlin.Triple;

public class Tetris extends SurfaceView implements SurfaceHolder.Callback, View.OnClickListener {
    private final Object lock = new Object();

    private boolean isPaused = false;

    private Playfield playfield = new Playfield();
    private Tetromino activePiece = new Tetromino(Tetromino.Shape.randomShape());
    private Tetromino.Shape nextPiece = Tetromino.Shape.randomShape();

    private DrawThread drawThread;
    private GameLogicThread gameLogicThread;
    private GameEventListener gameEventListener;

    private float POINT_SIZE, HORIZONTAL_BORDER, VERTICAL_BORDER, VERTICAL_OFFSET;

    @SuppressLint("ClickableViewAccessibility")
    public Tetris(Context context) {
        super(context);

        // In-game gesture handler
        setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeTop() {
                if (!isPaused) {
                    pauseThreads();
                    gameEventListener.onGameEvent(
                            new GameEvent.GamePause(gameLogicThread.gatherStatistics())
                    );
                }
            }

            @Override
            public void onSwipeBottom() {
                gameLogicThread.fastDrop();
            }

            @Override
            public void onSwipeRight() {
                gameLogicThread.moveActivePieceRight();
            }

            @Override
            public void onSwipeLeft() {
                gameLogicThread.moveActivePieceLeft();
            }
        });

        // Starts the game
        getHolder().addCallback(this);
    }

    // In-game buttons click handler
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRotateLeft:
                gameLogicThread.rotateActivePieceLeft();
                break;
            case R.id.btnRotateRight:
                gameLogicThread.rotateActivePieceRight();
                break;
            case R.id.btnMoveLeft:
                gameLogicThread.moveActivePieceLeft();
                break;
            case R.id.btnMoveRight:
                gameLogicThread.moveActivePieceRight();
                break;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        gameEventListener.onGameEvent(
                new GameEvent.NewNextPiece(nextPiece)
        );

        if ((getHeight() / 22f) > (getWidth() / 12f)) {
            POINT_SIZE = getWidth() / 12f;
            HORIZONTAL_BORDER = POINT_SIZE;
            VERTICAL_BORDER = (getHeight() / POINT_SIZE - 20) * POINT_SIZE / 2;
        } else {
            POINT_SIZE = getHeight() / 22f;
            HORIZONTAL_BORDER = (getWidth() / POINT_SIZE - 10) * POINT_SIZE / 2;
            VERTICAL_BORDER = POINT_SIZE;
        }

        VERTICAL_OFFSET = POINT_SIZE * 2;

        gameLogicThread = new GameLogicThread();
        drawThread = new DrawThread(getContext(), getHolder());
        gameLogicThread.updateTextViews();
        drawThread.start();
        gameLogicThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if ((height / 22f) > (width / 12f)) {
            POINT_SIZE = width / 12f;
            HORIZONTAL_BORDER = POINT_SIZE;
            VERTICAL_BORDER = (height / POINT_SIZE - 20) * POINT_SIZE / 2;
        } else {
            POINT_SIZE = height / 22f;
            HORIZONTAL_BORDER = (width / POINT_SIZE - 10) * POINT_SIZE / 2;
            VERTICAL_BORDER = POINT_SIZE;
        }

        VERTICAL_OFFSET = POINT_SIZE * 2;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        drawThread.requestStop();
        gameLogicThread.requestStop();
        resumeThreads();

        boolean retry = true;
        while (retry) {
            try {
                drawThread.join();
                gameLogicThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    // Game thread controllers
    public void stopThreads() {
        gameLogicThread.requestPause();
        drawThread.requestPause();
    }

    public void pauseThreads() {
        isPaused = true;
        gameLogicThread.requestPause();
        drawThread.requestPause();
    }

    public void resumeThreads() {
        isPaused = false;
        gameLogicThread.requestResume();
        drawThread.requestResume();
    }

    private class GameLogicThread extends Thread {
        private volatile int s = 0;
        private float speedMult = 1f;
        private volatile long speed = 1000;
        private volatile boolean isRunning = true;
        private int lines = 0, score = 0, level = 0;

        private List<Integer> clearLines() {
            List<Integer> currLineData = playfield.lineClear(), allLineData = currLineData;
            while (currLineData.size() > 0) {
                playfield.settleLines();
                currLineData = playfield.lineClear();
                allLineData = Stream.concat(allLineData.stream(), currLineData.stream())
                        .collect(Collectors.toList());
            }
            return allLineData;
        }

        private void updateGameValues(List<Integer> lineData) {
            for (int streak : lineData) {
                switch (streak) {
                    case 1:
                        score += 40 * (level + 1);
                        lines += 1;
                        break;
                    case 2:
                        score += 100 * (level + 1);
                        lines += 2;
                        break;
                    case 3:
                        score += 300 * (level + 1);
                        lines += 3;
                        break;
                    case 4:
                        score += 1200 * (level + 1);
                        lines += 4;
                        break;
                }
            }

            if (level < 10 && score > (level + 1) * 1800) {
                level += 1;
                speed = 1100 - (level + 1) * 100L;
            }

            updateTextViews();
        }

        // Helper methods
        public void updateTextViews() {
            gameEventListener.onGameEvent(
                    new GameEvent.ScoreUpdate(score)
            );
            gameEventListener.onGameEvent(
                    new GameEvent.LevelUpdate(level)
            );
            gameEventListener.onGameEvent(
                    new GameEvent.LinesUpdate(lines)
            );
        }

        public void updateImageView() {
            gameEventListener.onGameEvent(
                    new GameEvent.NewNextPiece(nextPiece)
            );
        }

        public GameStatistics gatherStatistics() {
            return new GameStatistics(level, score, lines);
        }

        // Active piece movements
        private void moveActivePieceDown() {
            activePiece.moveDown();
            if (playfield.hasCollision(activePiece)) {
                activePiece.moveUp();
                playfield.solidifyPiece(activePiece);
                getNewActivePiece();
                updateGameValues(clearLines());

                speedMult = 1f;
            }
        }

        public void moveActivePieceRight() {
            activePiece.moveRight();
            if (playfield.hasCollision(activePiece)) {
                activePiece.moveLeft();
            }
        }

        public void moveActivePieceLeft() {
            activePiece.moveLeft();
            if (playfield.hasCollision(activePiece)) {
                activePiece.moveRight();
            }
        }

        public void rotateActivePieceRight() {
            activePiece.rotateClockwise();
            if (playfield.hasCollision(activePiece)) {
                activePiece.rotateCounterClockwise();
            }
        }

        public void rotateActivePieceLeft() {
            activePiece.rotateCounterClockwise();
            if (playfield.hasCollision(activePiece)) {
                activePiece.rotateClockwise();
            }
        }

        // ToDo: Hard drop
        public void fastDrop() {
            speedMult = 0.1f;
            moveActivePieceDown();
        }

        private void getNewActivePiece() {
            activePiece = new Tetromino(nextPiece);
            nextPiece = Tetromino.Shape.randomShape();

            updateImageView();

            if (playfield.hasCollision(activePiece)) {
                stopGame();
            }
        }

        private void stopGame() {
            stopThreads();

            gameEventListener.onGameEvent(
                    new GameEvent.GameOver(gatherStatistics())
            );
        }

        // Game loop controls
        public void requestStop() {
            isRunning = false;
        }

        public void requestPause() {
            s = 1;
        }

        public void requestResume() {
            s = 0;
        }

        // Game loop ToDo: Synchronize with
        @Override
        public void run() {
//            synchronized (lock) {
                while (isRunning) {
                    while (s != 0);
//                    lock.notifyAll();
                    try {
                        moveActivePieceDown();
                        Thread.sleep((long) (speed * speedMult));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                }
            }
        }
    }

    private class DrawThread extends Thread {

        private volatile int s = 0;
        private final SurfaceHolder surfaceHolder;
        private final Paint paint = new Paint();
        private final Bitmap bitmap;
        private final Context context;
        private volatile boolean isRunning = true;

        public DrawThread(Context context, SurfaceHolder surfaceHolder) {
            this.context = context;
            this.surfaceHolder = surfaceHolder;

            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.point_overlay);
            bitmap = Bitmap.createScaledBitmap(bm, (int) POINT_SIZE, (int) POINT_SIZE, true);
        }

        // Render loop controls
        public void requestStop() {
            isRunning = false;
        }

        public void requestPause() {
            s = 1;
        }

        public void requestResume() {
            s = 0;
        }

        // Drawing logic
        private void drawPoint(int x, int y, Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(x * POINT_SIZE + HORIZONTAL_BORDER,
                    y * POINT_SIZE + VERTICAL_BORDER - VERTICAL_OFFSET,
                    (x + 1) * POINT_SIZE + HORIZONTAL_BORDER,
                    (y + 1) * POINT_SIZE + VERTICAL_BORDER - VERTICAL_OFFSET,
                    paint);
            canvas.drawBitmap(bitmap,
                    x * POINT_SIZE + HORIZONTAL_BORDER,
                    y * POINT_SIZE + VERTICAL_BORDER - VERTICAL_OFFSET,
                    paint);
        }

        private void drawBorders(Canvas canvas) {
            paint.setStrokeWidth(10);

            paint.setColor(0xff000000);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, getWidth(), VERTICAL_BORDER, paint);
            canvas.drawRect(0, 0, HORIZONTAL_BORDER, getHeight(), paint);
            canvas.drawRect(getWidth() - HORIZONTAL_BORDER, 0, getWidth(), getHeight(), paint);
            canvas.drawRect(0, getHeight() - VERTICAL_BORDER, getWidth(), getHeight(), paint);

            paint.setColor(0xffffffff);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(HORIZONTAL_BORDER, VERTICAL_BORDER, getWidth() - HORIZONTAL_BORDER, getHeight() - VERTICAL_BORDER, paint);
        }

        private void drawPlayfield(Canvas canvas) {
            paint.setColor(0xff222222);
            paint.setStrokeWidth(5);
            for (int x = 0; x < 10; x++) {
                canvas.drawLine(x * POINT_SIZE + HORIZONTAL_BORDER, 0 * POINT_SIZE + VERTICAL_BORDER,
                        x * POINT_SIZE + HORIZONTAL_BORDER, 20 * POINT_SIZE + VERTICAL_BORDER,
                        paint);
            }
            for (int y = 0; y < 22; y++) {
                canvas.drawLine(0 * POINT_SIZE + HORIZONTAL_BORDER, y * POINT_SIZE + VERTICAL_BORDER,
                        10 * POINT_SIZE + HORIZONTAL_BORDER, y * POINT_SIZE + VERTICAL_BORDER,
                        paint);
            }

            for (int y = 0; y < 22; y++) {
                for (int x = 0; x < 10; x++) {
                    if (playfield.playfieldState[y][x] != 0xff000000) {
                        paint.setStrokeWidth(10);
                        paint.setColor(playfield.playfieldState[y][x]);
                        drawPoint(x, y, canvas);
                    }
                }
            }
        }

        private void drawTetromino(Tetromino t, Canvas canvas) {
            paint.setColor(t.getColor());
            for (int y = 0; y < t.getShapeMatrix().length; y++) {
                for (int x = 0; x < t.getShapeMatrix()[y].length; x++) {
                    if (t.getShapeMatrix()[y][x] == 1) {
                        drawPoint(t.getX() + x, t.getY() + y, canvas);
                    }
                }
            }
        }

        // Render loop
        @Override
        public void run() {
//            synchronized (lock) {
                while (isRunning) {
                    while (s != 0);
//                    lock.notifyAll();
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        try {
                            canvas.drawColor(0xff000000);
                            drawPlayfield(canvas);
                            drawTetromino(activePiece, canvas);
                            drawBorders(canvas);
                        } finally {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
//                }
            }
        }
    }

    // Events to communicate with activity
    public interface GameEventListener {
        void onGameEvent(GameEvent event);
    }

    public static abstract class GameEvent {
        public abstract GameEventType getType();

        public static class ScoreUpdate extends GameEvent {
            private final int score;

            private ScoreUpdate(int score) {
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

            private LinesUpdate(int lines) {
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

            private LevelUpdate(int level) {
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

            private NewNextPiece(Tetromino.Shape nextShape) {
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
            private final GameStatistics statistics;

            public GameOver(GameStatistics statistics) {
                this.statistics = statistics;
            }

            public GameStatistics getPayload() {
                return statistics;
            }

            @Override
            public GameEventType getType() {
                return GameEventType.GAME_OVER;
            }
        }

        public static class GamePause extends GameEvent {
            private final GameStatistics statistics;

            public GamePause(GameStatistics statistics) {
                this.statistics = statistics;
            }

            public GameStatistics getPayload() {
                return statistics;
            }

            @Override
            public GameEventType getType() {
                return GameEventType.GAME_PAUSE;
            }
        }
    }

    public enum GameEventType {
        SCORE_UPDATE, LEVEL_UPDATE, NEXT_PIECE, GAME_OVER, GAME_PAUSE, LINES_UPDATE;
    }

    public void setGameEventListener(GameEventListener listener) {
        gameEventListener = listener;
    }

    public static class GameStatistics implements Parcelable {
        private final int level, score, lines;



        public GameStatistics(int level, int score, int lines) {
            this.level = level;
            this.score = score;
            this.lines = lines;
        }

        protected GameStatistics(Parcel in) {
            level = in.readInt();
            lines = in.readInt();
            score = in.readInt();
        }

        public static final Creator<GameStatistics> CREATOR = new Creator<GameStatistics>() {
            @Override
            public GameStatistics createFromParcel(Parcel in) {
                return new GameStatistics(in);
            }

            @Override
            public GameStatistics[] newArray(int size) {
                return new GameStatistics[size];
            }
        };

        public int getLevel() {
            return level;
        }

        public int getScore() {
            return score;
        }

        public int getLines() {
            return lines;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(level);
            dest.writeInt(lines);
            dest.writeInt(score);
        }
    }
}
