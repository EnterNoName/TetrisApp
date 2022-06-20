package com.example.tetris.Activities;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.tetris.Fragments.GameOverFragment;
import com.example.tetris.Fragments.PauseFragment;
import com.example.tetris.Models.Tetris;
import com.example.tetris.Models.Tetromino;
import com.example.tetris.R;

public class GameActivity extends AppCompatActivity {
    ImageView ivNextPiece;
    TextView tvScore, tvLines, tvLevel;
    ImageButton rotateLeft, rotateRight, moveLeft, moveRight;
    public Tetris game;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        game = new Tetris(getApplicationContext());
        ConstraintLayout layout = findViewById(R.id.layout);
        layout.addView(game, 0);

        game.setGameEventListener(event -> {
            switch (event.getType()) {
                case LEVEL_UPDATE:
                    int level = ((Tetris.GameEvent.LevelUpdate) event).getPayload();
                    runOnUiThread(() -> {
                        tvLevel.setText(getString(R.string.level, level));
                    });
                    break;
                case SCORE_UPDATE:
                    int score = ((Tetris.GameEvent.ScoreUpdate) event).getPayload();
                    runOnUiThread(() -> {
                        tvScore.setText(getString(R.string.score, score));
                    });
                    break;
                case LINES_UPDATE:
                    int lines = ((Tetris.GameEvent.LinesUpdate) event).getPayload();
                    runOnUiThread(() -> {
                        tvLines.setText(getString(R.string.lines, lines));
                    });
                    break;
                case NEXT_PIECE:
                    Tetromino.Shape nextPiece = ((Tetris.GameEvent.NewNextPiece) event).getPayload();
                    runOnUiThread(() -> {
                        switch (nextPiece) {
                            case O: // O-Shape
                                ivNextPiece.setImageResource(R.drawable.o_tetromino);
                                break;
                            case I: // I-Shape
                                ivNextPiece.setImageResource(R.drawable.i_tetromino);
                                break;
                            case T: // T-Shape
                                ivNextPiece.setImageResource(R.drawable.t_tetromino);
                                break;
                            case J: // J-Shape
                                ivNextPiece.setImageResource(R.drawable.j_tetromino);
                                break;
                            case L: // L-Shape
                                ivNextPiece.setImageResource(R.drawable.l_tetromino);
                                break;
                            case Z: // Z-Shape
                                ivNextPiece.setImageResource(R.drawable.z_tetromino);
                                break;
                            case S: // S-Shape
                                ivNextPiece.setImageResource(R.drawable.s_tetromino);
                                break;
                        }
                    });
                    break;
                case GAME_PAUSE: {
                    Tetris.GameStatistics stats = ((Tetris.GameEvent.GamePause) event).getPayload();
                    Bundle args = new Bundle();
                    args.putParcelable("statistics", stats);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragment_container_view, PauseFragment.class, args)
                            .commit();
                    break;
                }
                case GAME_OVER: {
                    Tetris.GameStatistics stats = ((Tetris.GameEvent.GameOver) event).getPayload();
                    Bundle args = new Bundle();
                    args.putParcelable("statistics", stats);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragment_container_view, GameOverFragment.class, args)
                            .commit();
                    break;
                }
            }
        });

        rotateLeft = findViewById(R.id.btnRotateLeft);
        rotateRight = findViewById(R.id.btnRotateRight);

        moveLeft = findViewById(R.id.btnMoveLeft);
        moveRight = findViewById(R.id.btnMoveRight);

        ivNextPiece = findViewById(R.id.ivNextPiece);
        tvLevel = findViewById(R.id.tvLevel);
        tvLines = findViewById(R.id.tvLines);
        tvScore = findViewById(R.id.tvScore);

        rotateLeft.setOnClickListener(game);
        rotateRight.setOnClickListener(game);
        moveLeft.setOnClickListener(game);
        moveRight.setOnClickListener(game);

        // Hiding device navigation
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
