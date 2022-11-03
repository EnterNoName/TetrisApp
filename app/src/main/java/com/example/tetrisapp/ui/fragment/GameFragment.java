package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.GameFragmentBinding;
import com.example.tetrisapp.model.Tetris;
import com.example.tetrisapp.model.configuration.PieceConfiguration;
import com.example.tetrisapp.model.configuration.PieceConfigurationImpl;
import com.example.tetrisapp.ui.view.GameView;
import com.example.tetrisapp.util.TouchListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameFragment extends Fragment {
    private final PieceConfiguration configuration = new PieceConfigurationImpl();
    private GameFragmentBinding binding;
    private Tetris game;
    private GameView surfaceView;

    private static final int MOVEMENT_INTERVAL = 125;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> futureMoveRight = null;
    private ScheduledFuture<?> futureMoveLeft = null;

    private ScheduledFuture<?> futureCountdown = null;
    private int countdownTimer = 0;

    private Animation scaleAnim;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = GameFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (game == null) {
            game = new Tetris(configuration, new String[]{"I", "J", "L", "T"}, new String[]{"Z", "S", "Z", "S"});
        }
        scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.scale);
        binding.gameView.setGame(game);
        game.setCallback(this::updateViews);
        initOnClickListeners();
        updateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        game.setPause(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        countdown();
    }

    private void countdown() {
        countdownTimer = 3;
        binding.tvCountdown.setVisibility(View.INVISIBLE);
        futureCountdown = executor.scheduleAtFixedRate(new CountdownRunnable(), 0, 1, TimeUnit.SECONDS);
    }

    private void confirmExit() {
        game.setPause(true);
        new MaterialAlertDialogBuilder(requireContext(), R.style.LightDialogTheme)
                .setTitle(getString(R.string.exit_dialog_title))
                .setMessage(getString(R.string.exit_dialog_description))
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> {
                    game.setPause(false);
                })
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> {
                    Navigation.findNavController(binding.getRoot()).popBackStack();
                })
                .show();
    }

    @SuppressLint("SetTextI18n")
    private void updateViews() {
        binding.include.pvNext1.setPiece(configuration.get(game.getTetrominoSequence().get(0)).copy());
        binding.include.pvNext2.setPiece(configuration.get(game.getTetrominoSequence().get(1)).copy());
        binding.include.pvNext3.setPiece(configuration.get(game.getTetrominoSequence().get(2)).copy());
        binding.include.pvNext4.setPiece(configuration.get(game.getTetrominoSequence().get(3)).copy());
        if (game.getHeldPiece() != null) {
            binding.include.pvHold.setPiece(game.getHeldPiece().copy());
        }

        binding.include.score.setText(game.getScore() + "");
        binding.include.level.setText(game.getLevel() + "");
        binding.include.lines.setText(game.getLines() + "");
        binding.include.combo.setText(game.getCombo() + "");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnLeft.setOnTouchListener(new TouchListener(getContext()) {
            @Override
            public void onTapDown() {
                futureMoveLeft = executor.scheduleAtFixedRate(new MoveLeftRunnable(), 0, MOVEMENT_INTERVAL, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onTapUp() {
                if (futureMoveLeft != null) futureMoveLeft.cancel(true);
            }
        });
        binding.btnRight.setOnTouchListener(new TouchListener(getContext()) {
            @Override
            public void onTapDown() {
                futureMoveRight = executor.scheduleAtFixedRate(new MoveRightRunnable(), 0, MOVEMENT_INTERVAL, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onTapUp() {
                if (futureMoveRight != null) futureMoveRight.cancel(true);
            }
        });
        binding.btnRotateLeft.setOnClickListener(v -> game.rotateTetrominoLeft());
        binding.btnRotateRight.setOnClickListener(v -> game.rotateTetrominoRight());
        binding.btnPause.setOnClickListener(v -> {
//            game.setPause(true);
            Navigation.findNavController(v).navigate(R.id.action_gameFragment_to_pauseFragment);
        });
        binding.btnDown.setOnTouchListener(new TouchListener(getContext()) {
            @Override
            public void onDoubleTap() {
                game.hardDrop();
            }

            @Override
            public void onTapDown() {
                game.setSoftDrop(true);
            }

            @Override
            public void onTapUp() {
                game.setSoftDrop(false);
            }
        });
        binding.include.cvHold.setOnClickListener(v -> game.hold());
    }

    private class MoveRightRunnable implements Runnable {
        @Override
        public void run() {
            game.moveTetrominoRight();
        }
    }

    private class MoveLeftRunnable implements Runnable {
        @Override
        public void run() {
            game.moveTetrominoLeft();
        }
    }

    private class CountdownRunnable implements Runnable {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (countdownTimer <= 0) {
                binding.tvCountdown.setText("GO!");
                binding.tvCountdown.startAnimation(scaleAnim);
                executor.schedule(new CountdownFinishRunnable(), 0, TimeUnit.MILLISECONDS);
            } else {
                binding.tvCountdown.setText(countdownTimer + "");
                binding.tvCountdown.startAnimation(scaleAnim);
                countdownTimer--;
            }
        }
    }

    private class CountdownFinishRunnable implements Runnable {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            binding.tvCountdown.setVisibility(View.INVISIBLE);
            game.setPause(false);
            futureCountdown.cancel(true);
        }
    }
}
