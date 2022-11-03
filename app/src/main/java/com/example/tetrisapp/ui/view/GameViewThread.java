package com.example.tetrisapp.ui.view;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameViewThread extends Thread {
    private final SurfaceHolder surfaceHolder;
    private final GameView surfaceView;
    private boolean threadRunning = false;

    public GameViewThread(SurfaceHolder surfaceHolder, GameView surfaceView) {
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
                synchronized (surfaceView.getGame().getPlayfield()) {
                    surfaceView.draw(c);
                }
            }

            if (c != null) {
                surfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }
}
