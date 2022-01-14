package com.example.tetris.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tetris.R;
import com.example.tetris.Utils.JSON;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvHighScore;
    Button btnStart, btnStats, btnQuit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnStart = findViewById(R.id.btnStart);
        btnStats = findViewById(R.id.btnStats);
        btnQuit = findViewById(R.id.btnQuit);

        tvHighScore = findViewById(R.id.highScoreMain);

        btnStart.setOnClickListener(this);
        btnStats.setOnClickListener(this);
        btnQuit.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int highScore = 0;
        JSONObject data = JSON.read(getApplicationContext(), "save.json");
        try {
            if (!data.isNull("bestAttempts")) {
                highScore = data.getJSONArray("bestAttempts").getJSONObject(0).getInt("score");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tvHighScore.setText(getResources().getString(R.string.current_high_score, highScore));
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btnStart:
                i = new Intent(MainActivity.this, GameActivity.class);
                startActivity(i);
                break;
            case R.id.btnStats:
                i = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(i);
                break;
            case R.id.btnQuit:
                finishAndRemoveTask();
                break;
        }
    }
}