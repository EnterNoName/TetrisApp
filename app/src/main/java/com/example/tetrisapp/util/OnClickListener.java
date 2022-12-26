package com.example.tetrisapp.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import com.example.tetrisapp.R;
import com.example.tetrisapp.ui.activity.MainActivity;

import javax.inject.Inject;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

public class OnClickListener implements View.OnTouchListener {
    private long mLastClickTime = 0;
    private int soundResId = R.raw.click;
    private final MainActivity activity;

    public OnClickListener(MainActivity activity) {
        this.activity = activity;
    }

    public OnClickListener setSound(int resId) {
        this.soundResId = resId;
        return this;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activity.hide();
                break;
            case MotionEvent.ACTION_UP:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return true;
                mLastClickTime = SystemClock.elapsedRealtime();

                MediaHelper mediaHelper = new MediaHelper(activity.getApplicationContext());
                mediaHelper.playSound(R.raw.click);

                v.performClick();
                break;
            default:
                break;
        }
        return true;
    }
}
