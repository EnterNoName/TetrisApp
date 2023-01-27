package com.example.tetrisapp.util;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import com.example.tetrisapp.R;
import com.example.tetrisapp.ui.activity.MainActivity;

public class OnTouchListener implements View.OnTouchListener {
    private long mLastClickTime = 0;
    private int soundResId = R.raw.click;
    private final MainActivity activity;

    public OnTouchListener(MainActivity activity) {
        this.activity = activity;
    }

    public OnTouchListener setSound(int resId) {
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

                MediaPlayerUtil mediaHelper = new MediaPlayerUtil(activity.getApplicationContext());
                float volume = activity.getPreferences(Context.MODE_PRIVATE).getInt(activity.getString(R.string.setting_sfx_volume), 5) / 10f;
                mediaHelper.playSound(soundResId, volume);

                v.performClick();
                break;
            default:
                break;
        }
        return true;
    }
}
