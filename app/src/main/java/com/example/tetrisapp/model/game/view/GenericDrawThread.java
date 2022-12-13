package com.example.tetrisapp.model.game.view;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GenericDrawThread<T extends SurfaceView> extends Thread {
    private final SurfaceHolder surfaceHolder;
    private final T surfaceView;
    private boolean threadRunning = false;
    private Object lock = null;

    public GenericDrawThread(SurfaceHolder surfaceHolder, T surfaceView) {
        this.surfaceHolder = surfaceHolder;
        this.surfaceView = surfaceView;
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
                if (lock != null) {
                    synchronized (lock) {
                        surfaceView.draw(c);
                    }
                } else {
                    surfaceView.draw(c);
                }
            }

            if (c != null) {
                surfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }

    public void setLock(Object lock) {
        this.lock = lock;
    }
}
