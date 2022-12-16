package com.example.tetrisapp.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.tetrisapp.R;
import com.example.tetrisapp.model.game.Piece;

public class PieceView extends View {
    private final Paint paint = new Paint();
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

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions(w, h);
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
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
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
        invalidate();
    }
}
