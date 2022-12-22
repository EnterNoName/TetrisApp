package com.example.tetrisapp.ui.activity;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.local.db.AppDatabase;
import com.example.tetrisapp.data.remote.UpdateService;
import com.example.tetrisapp.databinding.ActivityMainBinding;
import com.example.tetrisapp.util.Singleton;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private MediaPlayer mainThemeMP;
    private MediaPlayer clickMP;
    private MediaPlayer countdownMP;
    private MediaPlayer solidifyMP;
    private MediaPlayer gameStartMP;
    private MediaPlayer gameOverMP;
    private MediaPlayer gameStartBtnMP;

    private Retrofit retrofit;
    private UpdateService updateService;

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= 30) {
                mContentView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };

    private final Runnable mShowPart2Runnable = () -> {
        // Delayed display of UI elements
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };

    private boolean mVisible;
    private final Runnable mHideRunnable = () -> hide();

    private final View.OnTouchListener mDelayHideTouchListener = (view, motionEvent) -> {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hide();
                break;
            case MotionEvent.ACTION_UP:
                view.performClick();
                break;
            default:
                break;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContentView = binding.fragmentContainerView;
        mContentView.setOnTouchListener(mDelayHideTouchListener);

        mainThemeMP = MediaPlayer.create(this, R.raw.main);
        countdownMP = MediaPlayer.create(this, R.raw.countdown);
        gameStartMP = MediaPlayer.create(this, R.raw.gamestart);
        gameOverMP = MediaPlayer.create(this, R.raw.gameover);
        solidifyMP = MediaPlayer.create(this, R.raw.solidify);
        clickMP = MediaPlayer.create(this, R.raw.click);
        gameStartBtnMP = MediaPlayer.create(this, R.raw.gamestartbtn);

        retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.update_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        updateService = retrofit.create(UpdateService.class);

        mainThemeMP.setLooping(true);

        Singleton.INSTANCE.setDb(Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "db").build());
    }

    @Override
    protected void onDestroy() {
        mainThemeMP.release();
        countdownMP.release();
        gameOverMP.release();
        gameStartBtnMP.release();
        gameStartMP.release();
        clickMP.release();
        solidifyMP.release();
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hide();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            mContentView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public MediaPlayer getMainThemeMP() {
        return mainThemeMP;
    }

    public MediaPlayer getClickMP() {
        return clickMP;
    }

    public MediaPlayer getCountdownMP() {
        return countdownMP;
    }

    public MediaPlayer getSolidifyMP() {
        return solidifyMP;
    }

    public MediaPlayer getGameStartMP() {
        return gameStartMP;
    }

    public MediaPlayer getGameOverMP() {
        return gameOverMP;
    }

    public MediaPlayer getGameStartBtnMP() {
        return gameStartBtnMP;
    }

    public UpdateService getUpdateService() {
        return updateService;
    }
}