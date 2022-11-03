package com.example.tetrisapp.ui.view;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.example.tetrisapp.model.Piece;

public class PieceViewThread extends Thread {
    private final SurfaceHolder surfaceHolder;
    private final PieceView surfaceView;
    private boolean threadRunning = false;

    public PieceViewThread(SurfaceHolder surfaceHolder, PieceView surfaceView) {
        this.surfaceHolder = surfaceHolder;
        this.surfaceView = surfaceView;
    }

    public void setRunning(boolean b) {
        threadRunning = b;
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        while (threadRunning) {
            Canvas c = null;
            c = surfaceHolder.lockCanvas(null);
            synchronized (surfaceHolder) {
                surfaceView.draw(c);
            }
            if (c != null) {
                surfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }
}
