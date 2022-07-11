package com.example.tetris.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.tetris.R;
import com.example.tetris.model.Playfield;
import com.example.tetris.model.Tetromino;
import com.example.tetris.util.GameEvent;
import com.example.tetris.util.GameEventListener;
import com.example.tetris.util.OnSwipeTouchListener;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tetris extends SurfaceView implements SurfaceHolder.Callback {
    private final Object lock = new Object();

    // Game values
    private boolean isSoftDrop = false;
    private volatile long speed = 1000;
    private volatile boolean isRunning = true;
    private int lines = 0, score = 0, level = 1;

    private boolean isPaused = false;

    private Playfield playfield = new Playfield();
    private Tetromino activePiece = new Tetromino(Tetromino.Shape.randomShape());
    private Tetromino.Shape nextPiece = Tetromino.Shape.randomShape();

    private DrawThread drawThread;
    private GameLogicThread gameLogicThread;
    private GameEventListener gameEventListener;

    private float POINT_SIZE, HORIZONTAL_BORDER, VERTICAL_BORDER, VERTICAL_OFFSET;

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
                if (!isPaused) {
                    if (isSoftDrop) {
                        gameLogicThread.hardDrop();
                    } else {
                        gameLogicThread.softDrop();
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                if (!isPaused) {
                    gameLogicThread.moveActivePieceRight();
                }
            }

            @Override
            public void onSwipeLeft() {
                if (!isPaused) {
                    gameLogicThread.moveActivePieceLeft();
                }
            }
        });

        // Starts the game
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        configureDimensions(getWidth(), getHeight());

        startThreads();

        gameEventListener.onGameEvent(
                new GameEvent.NewNextPiece(nextPiece)
        );
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        configureDimensions(width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopThreads();
    }

    private void configureDimensions(int width, int height) {
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

    // Game controllers
    private void stopGame(GameStatistics gameStatistics) {
        pauseThreads();

        gameEventListener.onGameEvent(
                new GameEvent.GameOver(gameStatistics)
        );
    }

    public void startThreads() {
        isRunning = true;
        gameLogicThread = new GameLogicThread();
        drawThread = new DrawThread(getContext(), getHolder());
        gameLogicThread.updateTextViews();
        drawThread.start();
        gameLogicThread.start();
    }

    public void stopThreads() {
        isRunning = false;
        boolean retry = true;
        while (retry) {
            try {
                gameLogicThread.join(10);
                drawThread.join(10);
                retry = false;
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void pauseThreads() {
        isPaused = true;
    }

    public void resumeThreads() {
        isPaused = false;
    }

    public void setGameEventListener(GameEventListener listener) {
        gameEventListener = listener;
    }

    public static class GameStatistics implements Parcelable {
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

    private class GameLogicThread extends Thread {
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

            if (level < 10 && score > level * 1800) {
                level = Math.min(10, score / 1800);
                speed = 900 - (level - 1) * 85L;
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

        public void softDrop() {
            isSoftDrop = true;
        }

        public void hardDrop() {
            while (true) {
                activePiece.moveDown();
                if (playfield.hasCollision(activePiece)) {
                    activePiece.moveUp();
                    playfield.solidifyPiece(activePiece);
                    getNewActivePiece();
                    updateGameValues(clearLines());
                    break;
                }
            }
        }

        private void getNewActivePiece() {
            isSoftDrop = false;

            activePiece = new Tetromino(nextPiece);
            nextPiece = Tetromino.Shape.randomShape();

            updateImageView();

            if (playfield.hasCollision(activePiece)) {
                stopGame(gatherStatistics());
            }
        }

        // Game loop ToDo: Synchronize with
        @Override
        public void run() {
            while (isRunning) {
                if (!isPaused) {
                    try {
                        moveActivePieceDown();
                        if (isSoftDrop) {
                            sleep((long) (speed * 0.1f));
                        } else {
                            sleep(speed);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class DrawThread extends Thread {

        private final SurfaceHolder surfaceHolder;
        private final Paint paint = new Paint();
        private final Bitmap bitmap;
        private final Context context;

        public DrawThread(Context context, SurfaceHolder surfaceHolder) {
            this.context = context;
            this.surfaceHolder = surfaceHolder;

            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.point_overlay);
            bitmap = Bitmap.createScaledBitmap(bm, (int) POINT_SIZE, (int) POINT_SIZE, true);
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
            while (isRunning) {
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
            }
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void rotateActivePieceLeft() {
        gameLogicThread.rotateActivePieceLeft();
    }

    public void rotateActivePieceRight() {
        gameLogicThread.rotateActivePieceRight();
    }

    public void moveActivePieceLeft() {
        gameLogicThread.moveActivePieceLeft();
    }

    public void moveActivePieceRight() {
        gameLogicThread.moveActivePieceRight();
    }
}
