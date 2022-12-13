package com.example.tetrisapp.model.game.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.tetrisapp.R;
import com.example.tetrisapp.model.game.Piece;

public class PieceView extends SurfaceView implements SurfaceHolder.Callback {
    private final Paint paint = new Paint();
    private GenericDrawThread<PieceView> thread;
    private Piece piece = null;

    private int pointSize;
    private int xOffset;
    private int yOffset;

    private int color = 0xff000000;

    public PieceView(Context context, @Nullable AttributeSet attrs) {
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
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        thread = new GenericDrawThread<>(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        calculateDimensions(width, height);
    }

    private void calculateDimensions(int width, int height) {
        if (piece != null) {
            int pieceHeight = -1;
            int pieceWidth = -1;

            for (int y = 0; y < piece.getMatrix().length; y++) {
                for (int x = 0; x < piece.getMatrix()[y].length; x++) {
                    if (piece.getMatrix()[y][x] == 1) {
                        pieceHeight = Math.max(pieceHeight, y + 1);
                        pieceWidth = Math.max(pieceWidth, x + 1);
                    }
                }
            }

            if (height / pieceHeight <= width / pieceWidth) {
                pointSize = height / pieceHeight;
            } else {
                pointSize = width / pieceWidth;
            }

            xOffset = (width - pointSize * pieceWidth) / 2;
            yOffset = (height - pointSize * pieceHeight) / 2;
        }
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
        if (canvas != null) {
            super.draw(canvas);
            canvas.drawColor(color);
            if (piece != null) {
                drawTetromino(canvas);
            }
        }
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

        VectorDrawableCompat overlay = VectorDrawableCompat.create(getResources(), overlayResource, null);
        assert overlay != null;
        overlay.mutate();
        overlay.setBounds(0, 0, pointSize, pointSize);
        canvas.translate(x * pointSize + xOffset, y * pointSize + yOffset);
        overlay.draw(canvas);
        canvas.translate(-(x * pointSize + xOffset), -(y * pointSize + yOffset));
    }

    private void drawTetromino(Canvas canvas) {
        for (int y = 0; y < piece.getMatrix().length; y++) {
            if (y >= piece.getMatrix().length) continue;
            for (int x = 0; x < piece.getMatrix()[y].length; x++) {
                if (x < piece.getMatrix()[y].length && piece.getMatrix()[y][x] == 1) {
                    drawPoint(x, y, piece.getColor(), piece.getOverlayResId(), canvas);
                }
            }
        }
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
        calculateDimensions(getWidth(), getHeight());
    }
}
