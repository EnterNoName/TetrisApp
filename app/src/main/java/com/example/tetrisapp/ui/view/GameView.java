package com.example.tetrisapp.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.graphics.ColorUtils;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.tetrisapp.R;
import com.example.tetrisapp.model.game.Piece;
import com.example.tetrisapp.model.game.Tetris;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameView extends View {
    private final Paint paint = new Paint();
    private Tetris game = null;

    private int pointSize;
    private int xOffset;
    private int yOffset;
    private final int generalOffset = 20;
    private final int borderWidth = 5;

    private int color = 0xff000000;
    private static final int SHADOW_COLOR = 0x11000000;
    private static final int FPS = 120;

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

    private void init() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this::postInvalidate, 0, 1000 / FPS, TimeUnit.MILLISECONDS);
    }

    public void setGame(Tetris game) {
        this.game = game;
    }

    public Tetris getGame() {
        return game;
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game != null && canvas != null) {
            canvas.drawColor(color);
            drawGrid(canvas);
            drawPlayfield(canvas);
            drawShadow(canvas);
            drawTetromino(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions(w, h);
    }

    private void drawShadow(Canvas canvas) {
        Piece tetromino = game.getShadow();

        for (int y = 0; y < tetromino.getMatrix().length; y++) {
            for (int x = 0; x < tetromino.getMatrix()[y].length; x++) {
                if (tetromino.getMatrix()[y][x] == 1 & (tetromino.getRow() + y - 2) >= 0) {
                    drawPoint(tetromino.getCol() + x, tetromino.getRow() + y - 2, SHADOW_COLOR, tetromino.getOverlayResId(), canvas);
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
        int tetrominoColor = tetromino.getColor();

        int delay = (int) game.getDelay();
        if (game.isLockDelay() && delay > 0) {
            float x = delay * 1f / Tetris.LOCK_DELAY;
            tetrominoColor = ColorUtils.blendARGB(
                    tetrominoColor,
                    0xFFFFFFFF,
                    (float) (-2 * Math.pow(x, 2) + 2 * x + 0.5f));
        }

        for (int y = 0; y < tetromino.getMatrix().length; y++) {
            for (int x = 0; x < tetromino.getMatrix()[y].length; x++) {
                if (tetromino.getMatrix()[y][x] == 1 & (tetromino.getRow() + y - 2) >= 0) {
                    drawPoint(tetromino.getCol() + x, tetromino.getRow() + y - 2, tetrominoColor, tetromino.getOverlayResId(), canvas);
                }
            }
        }
    }
}
