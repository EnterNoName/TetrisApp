package com.example.tetrisapp.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.tetrisapp.R;
import com.example.tetrisapp.model.game.Piece;
import com.example.tetrisapp.model.game.Tetris;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private final Paint paint = new Paint();
    private GameViewThread thread;
    private Tetris game = null;

    private int pointSize;
    private int xOffset;
    private int yOffset;
    private final int generalOffset = 20;
    private final int borderWidth = 5;

    private int color = 0xff000000;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BackgroundView,
                0, 0);
        try {
            color = a.getColor(R.styleable.BackgroundView_backgroundColor, color);
        } finally {
            a.recycle();
        }

        init();
    }

    public void setGame(Tetris game) {
        this.game = game;
    }

    public Tetris getGame() {
        return game;
    }

    private void init() {
        getHolder().addCallback(this);
    }

    private void calculateDimensions(int width, int height) {
        if ((height - generalOffset * 2) / 20 <= (width - generalOffset * 2) / 10) {
            pointSize = (height - generalOffset * 2) / 20;
        } else {
            pointSize = (width - generalOffset * 2) / 10;
        }

        xOffset = (width - pointSize * 10) / 2;
        yOffset = (height - pointSize * 20) / 2;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        thread = new GameViewThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        calculateDimensions(width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (game != null && canvas != null) {
            super.draw(canvas);
            canvas.drawColor(color);
            drawGrid(canvas);
            drawPlayfield(canvas);
            drawShadow(canvas);
            drawTetromino(canvas);
        }
    }

    private void drawShadow(Canvas canvas) {
        Piece tetromino = game.getCurrentPiece();

        int yOffset = 0;
        while (game.getPlayfield().isValidMove(tetromino.getMatrix(), tetromino.getRow() + yOffset + 1, tetromino.getCol())) {
            yOffset++;
        }

        for (int y = 0; y < tetromino.getMatrix().length; y++) {
            for (int x = 0; x < tetromino.getMatrix()[y].length; x++) {
                if (tetromino.getMatrix()[y][x] == 1 & (tetromino.getRow() + y - 2 + yOffset) >= 0) {
                    drawPoint(tetromino.getCol() + x, tetromino.getRow() + y - 2 + yOffset, 0x11000000, tetromino.getOverlayResId(), canvas);
                }
            }
        }
    }

    private void drawGrid(Canvas canvas) {
        paint.setColor(0xFF666666);
        paint.setStrokeWidth(borderWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);

        for (int x = 0; x <= 10; x++) {
            canvas.drawLine(pointSize * x + xOffset,
                    yOffset,
                    pointSize * x + xOffset,
                    getHeight() - yOffset,
                    paint);
        }

        for (int y = 0; y <= 20; y++) {
            canvas.drawLine(xOffset,
                    pointSize * y + yOffset,
                    getWidth() - xOffset,
                    pointSize * y + yOffset,
                    paint);
        }

        paint.setStrokeCap(Paint.Cap.SQUARE);
    }

    private void drawPoint(int x, int y, int color, int overlayResource, Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        paint.setColor(color);

        Rect point = new Rect(x * pointSize + xOffset,
                y * pointSize + yOffset,
                (x + 1) * pointSize + xOffset,
                (y + 1) * pointSize + yOffset);

        canvas.drawRect(point, paint);

        VectorDrawableCompat overlay = VectorDrawableCompat.create(getContext().getResources(), overlayResource, null);
        assert overlay != null;
        overlay.mutate();
        overlay.setBounds(0, 0, pointSize, pointSize);
        canvas.translate(x * pointSize + xOffset, y * pointSize + yOffset);
        overlay.draw(canvas);
        canvas.translate(-(x * pointSize + xOffset), -(y * pointSize + yOffset));
    }

    private void drawPlayfield(Canvas canvas) {
        for (int y = 2; y < 22; y++) {
            for (int x = 0; x < 10; x++) {
                String point = game.getPlayfield().getState()[y][x];
                if (point != null) {
                    Piece piece = game.getConfiguration().get(point);
                    drawPoint(x, y - 2, piece.getColor(), piece.getOverlayResId(), canvas);
                }
            }
        }
    }

    private void drawTetromino(Canvas canvas) {
        Piece tetromino = game.getCurrentPiece();
        int color = tetromino.getColor();

        int delay = (int) game.getDelay();
        if (game.isLockDelay() && delay > 0) {
            color = ColorUtils.blendARGB(
                    color,
                    0xFFFFFFFF,
                    (float) (-Math.pow(delay / (Tetris.LOCK_DELAY * 20f) - 10, 2) + 100f));
        }

        for (int y = 0; y < tetromino.getMatrix().length; y++) {
            for (int x = 0; x < tetromino.getMatrix()[y].length; x++) {
                if (tetromino.getMatrix()[y][x] == 1 & (tetromino.getRow() + y - 2) >= 0) {
                    drawPoint(tetromino.getCol() + x, tetromino.getRow() + y - 2, color, tetromino.getOverlayResId(), canvas);
                }
            }
        }
    }
}
