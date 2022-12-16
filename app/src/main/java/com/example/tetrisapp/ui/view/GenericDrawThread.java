package com.example.tetrisapp.ui.view;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GenericDrawThread<T extends SurfaceView, S> extends Thread {
    private boolean threadRunning = false;
    private final SurfaceHolder surfaceHolder;
    private final T surfaceView;
    private final S lock;

    public GenericDrawThread(SurfaceHolder surfaceHolder, T surfaceView, S lock) {
        this.surfaceHolder = surfaceHolder;
        this.surfaceView = surfaceView;
        this.lock = lock;
    }

    public void setRunning(boolean b) {
        threadRunning = b;
    }

    @Override
    public void run() {
        while (threadRunning) {
            Canvas c = null;
            c = surfaceHolder.lockCanvas();
            synchronized (surfaceHolder) {
                surfaceView.draw(c);
            }

            if (c != null) {
                surfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }
}
