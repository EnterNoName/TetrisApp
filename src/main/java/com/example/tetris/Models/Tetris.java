package com.example.tetris.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.tetris.Utils.OnSwipeTouchListener;
import com.example.tetris.R;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tetris extends SurfaceView implements SurfaceHolder.Callback, View.OnClickListener {
    Playfield playfield = new Playfield();
    Handler ivHandler, tvHandler, gmHandler;
    Tetromino activePiece = new Tetromino(Tetromino.Shape.randomShape());
    Tetromino.Shape nextPiece = Tetromino.Shape.randomShape();

    DrawThread drawThread;
    GameLogicThread gameLogicThread;

    private float POINT_SIZE, HORIZONTAL_BORDER, VERTICAL_BORDER, VERTICAL_OFFSET;

    public Tetris(Context context, Handler ivHandler, Handler tvHandler, Handler gmHandler) {
        super(context);
        this.ivHandler = ivHandler;
        this.tvHandler = tvHandler;
        this.gmHandler = gmHandler;

        this.setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeTop() {
                gameLogicThread.stopGame();
            }

            @Override
            public void onSwipeBottom() {
                gameLogicThread.fastDrop();
            }
        });

        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Message msg = Message.obtain();
        msg.obj = nextPiece;
        ivHandler.sendMessage(msg);

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

        drawThread = new DrawThread(getContext(), getHolder());
        drawThread.start();

        gameLogicThread = new GameLogicThread();
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

    public class GameLogicThread extends Thread {
        private float speedMult = 1f;
        private volatile long speed = 900;
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

            if (level < 10 && score > (level + 1) * 2000) {
                level += 1;
                speed = 900 - (level + 1) * 50L;
            }

            updateTextViews();
        }

        private void updateTextViews() {
            Message msgScore = Message.obtain(), msgLevel = Message.obtain(), msgLines = Message.obtain();

            msgScore.what = 0;
            msgScore.obj = "Score: " + score;

            msgLevel.what = 1;
            msgLevel.obj = "Level: " + level;

            msgLines.what = 2;
            msgLines.obj = "Lines: " + lines;

            tvHandler.sendMessage(msgScore);
            tvHandler.sendMessage(msgLevel);
            tvHandler.sendMessage(msgLines);
        }

        public void moveActivePieceDown() {
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

        public void fastDrop() {
            speedMult = 0.1f;
            moveActivePieceDown();
        }

        private void getNewActivePiece() {
            activePiece = new Tetromino(nextPiece);
            nextPiece = Tetromino.Shape.randomShape();

            Message msg = Message.obtain();
            msg.obj = nextPiece;
            ivHandler.sendMessage(msg);

            if (playfield.hasCollision(activePiece)) {
                stopGame();
            }
        }

        public void stopGame() {
            gameLogicThread.requestStop();
            drawThread.requestStop();

            Message msgGameOver = Message.obtain();
            msgGameOver.obj = new int[]{score, lines, level};
            gmHandler.sendMessage(msgGameOver);
        }

        public void requestStop() {
            isRunning = false;
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    moveActivePieceDown();
                    Thread.sleep((long) (speed * speedMult));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class DrawThread extends Thread {

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

        public void requestStop() {
            isRunning = false;
        }

        public void drawPoint(int x, int y, Canvas canvas) {
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

        public void drawBorders(Canvas canvas) {
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

        public void drawPlayfield(Canvas canvas) {
            for (int y = 0; y < 22; y++) {
                for (int x = 0; x < 10; x++) {
                    if (playfield.playfieldState[y][x] != 0xff000000) {
                        paint.setColor(playfield.playfieldState[y][x]);
                        drawPoint(x, y, canvas);
                    }
                }
            }
        }

        public void drawTetromino(Tetromino t, Canvas canvas) {
            paint.setColor(t.getColor());
            for (int y = 0; y < t.getShapeMatrix().length; y++) {
                for (int x = 0; x < t.getShapeMatrix()[y].length; x++) {
                    if (t.getShapeMatrix()[y][x] == 1) {
                        drawPoint(t.getX() + x, t.getY() + y, canvas);
                    }
                }
            }
        }

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
}
