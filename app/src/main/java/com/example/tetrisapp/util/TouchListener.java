package com.example.tetrisapp.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class TouchListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    private static final int SWIPE_DISTANCE_THRESHOLD = 200;
    private static final int SWIPE_VELOCITY_THRESHOLD = 400;

    public TouchListener (Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener(){
            @Override
            public boolean onSwipe(Direction direction, float distance, float velocity) {
                if (distance >= SWIPE_DISTANCE_THRESHOLD && velocity >= SWIPE_VELOCITY_THRESHOLD) {
                    switch (direction) {
                        case up:
                            onSwipeUp();
                            break;
                        case down:
                            onSwipeDown();
                            break;
                        case left:
                            onSwipeLeft();
                            break;
                        case right:
                            onSwipeRight();
                            break;
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                TouchListener.this.onDoubleTap();
                return true;
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onTapDown();
                break;
            case MotionEvent.ACTION_UP:
                onTapUp();
                break;
        }

        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown() {
    }

    public void onDoubleTap() {
    }

    public void onTapDown() {
    }

    public void onTapUp() {
    }
}
