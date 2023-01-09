package com.example.tetrisapp.ui.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.FragmentGameBinding;
import com.example.tetrisapp.model.game.Tetris;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.ui.viewmodel.GameViewModel;
import com.example.tetrisapp.util.MediaPlayerUtil;
import com.example.tetrisapp.util.OnTouchListener;
import com.example.tetrisapp.util.OnGestureListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GameFragment extends Fragment {
    private static final String TAG = "GameFragment";
    private FragmentGameBinding binding;
    private GameViewModel viewModel;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    SharedPreferences preferences;

    private static final int MOVEMENT_INTERVAL = 125;

    private int countdown;
    private int countdownRemaining;

    private ScheduledFuture<?> futureMoveRight = null;
    private ScheduledFuture<?> futureMoveLeft = null;
    private Future<?> countdownFuture = null;

    private MediaPlayer gameMusic;
    @Inject MediaPlayerUtil mediaHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        countdown = preferences.getInt(getString(R.string.setting_countdown), 5);
        countdownRemaining = countdown;

        gameMusic = MediaPlayer.create(requireContext(), R.raw.main);
        gameMusic.setOnPreparedListener(MediaPlayer::pause);
        gameMusic.setVolume(0.5f, 0.5f);
        gameMusic.setLooping(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameBinding.inflate(inflater, container, false);
        binding.getRoot().setKeepScreenOn(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.gameView.setGame(viewModel.getGame());

        initOnClickListeners();
        initGameListeners();

        updateScoreboard();
        updatePieceViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        countdown();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.getGame().setPause(true);
    }

    private void countdown() {
        countdownFuture = executor.submit(new CountdownRunnable());
    }

    private void confirmExit() {
        viewModel.getGame().setPause(true);
        new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.exit_dialog_title))
                .setMessage(getString(R.string.exit_dialog_description))
                .setOnDismissListener((dialog) -> viewModel.getGame().setPause(false))
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> viewModel.getGame().setPause(false))
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameFragment_to_mainMenuFragment))
                .show();
    }

    @SuppressLint("SetTextI18n")
    private void updateScoreboard() {
        requireActivity().runOnUiThread(() -> {
            binding.include.score.setText(viewModel.getGame().getScore() + "");
            binding.include.level.setText(viewModel.getGame().getLevel() + "");
            binding.include.lines.setText(viewModel.getGame().getLines() + "");
            binding.include.combo.setText(viewModel.getGame().getCombo() + "");
        });

        float playbackSpeed = 2 - viewModel.getGame().getSpeed() / (float) Tetris.DEFAULT_SPEED;
        gameMusic.setPlaybackParams(new PlaybackParams().setSpeed(playbackSpeed));
    }

    private void updatePieceViews() {
        requireActivity().runOnUiThread(() -> {
            binding.include.pvNext1.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(0)).copy());
            binding.include.pvNext2.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(1)).copy());
            binding.include.pvNext3.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(2)).copy());
            binding.include.pvNext4.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(3)).copy());
        });

        requireActivity().runOnUiThread(() -> {
            if (viewModel.getGame().getHeldPiece() != null) {
                binding.include.pvHold.setPiece(viewModel.getGame().getHeldPiece().copy());
            }
        });
    }

    private void initGameListeners() {
        viewModel.getGame().setOnGameValuesUpdate(this::updateScoreboard);
        viewModel.getGame().setOnHold(this::updatePieceViews);
        viewModel.getGame().setOnMove(() -> binding.gameView.postInvalidate());
        viewModel.getGame().setOnSolidify(() -> {
            mediaHelper.playSound(R.raw.solidify);
            this.updatePieceViews();
        });
        viewModel.getGame().setOnGameOver(() -> {
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;

            mediaHelper.playSound(R.raw.gameover);

            GameFragmentDirections.ActionGameFragmentToGameOverFragment action = GameFragmentDirections.actionGameFragmentToGameOverFragment();
            action.setScore(viewModel.getGame().getScore());
            action.setLevel(viewModel.getGame().getLevel());
            action.setLines(viewModel.getGame().getLines());
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
        viewModel.getGame().setOnPause(() -> gameMusic.pause());
        viewModel.getGame().setOnResume(() -> gameMusic.start());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnLeft.setOnTouchListener(new OnGestureListener(getContext()) {
            @Override
            public void onTapDown() {
                futureMoveLeft = executor.scheduleAtFixedRate(new MoveLeftRunnable(), 0, MOVEMENT_INTERVAL, TimeUnit.MILLISECONDS);
                mediaHelper.playSound(R.raw.click);
            }

            @Override
            public void onTapUp() {
                if (futureMoveLeft != null) futureMoveLeft.cancel(true);
            }
        });

        binding.btnRight.setOnTouchListener(new OnGestureListener(getContext()) {
            @Override
            public void onTapDown() {
                futureMoveRight = executor.scheduleAtFixedRate(new MoveRightRunnable(), 0, MOVEMENT_INTERVAL, TimeUnit.MILLISECONDS);
                mediaHelper.playSound(R.raw.click);
            }

            @Override
            public void onTapUp() {
                if (futureMoveRight != null) futureMoveRight.cancel(true);
            }
        });

        binding.btnRotateLeft.setOnClickListener(v -> {
            viewModel.getGame().rotateTetrominoLeft();
            mediaHelper.playSound(R.raw.click);
        });

        binding.btnRotateRight.setOnClickListener(v -> {
            viewModel.getGame().rotateTetrominoRight();
            mediaHelper.playSound(R.raw.click);
        });

        binding.btnDown.setOnTouchListener(new OnGestureListener(getContext()) {
            @Override
            public void onDoubleTap() {
                viewModel.getGame().hardDrop();
            }

            @Override
            public void onTapDown() {
                viewModel.getGame().setSoftDrop(true);
                mediaHelper.playSound(R.raw.click);
            }

            @Override
            public void onTapUp() {
                viewModel.getGame().setSoftDrop(false);
            }
        });

        binding.include.cvHold.setOnClickListener(v -> {
            mediaHelper.playSound(R.raw.click);
            viewModel.getGame().hold();
        });

        binding.btnPause.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnPause.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_gameFragment_to_pauseFragment));
    }


    // Runnables
    private class MoveRightRunnable implements Runnable {
        @Override
        public void run() {
            viewModel.getGame().moveTetrominoRight();
        }
    }

    private class MoveLeftRunnable implements Runnable {
        @Override
        public void run() {
            viewModel.getGame().moveTetrominoLeft();
        }
    }

    private class CountdownRunnable implements Runnable {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            requireActivity().runOnUiThread(() -> {
                binding.tvCountdown.setVisibility(View.VISIBLE);
                animate(binding.tvCountdown, new Animator.AnimatorListener() {
                    public void updateUI() {
                        if (countdownRemaining > 0) {
                            binding.tvCountdown.setText(countdownRemaining + "");
                            mediaHelper.playSound(R.raw.countdown);
                        } else {
                            countdownRemaining = countdown;
                            binding.tvCountdown.setText("GO!");
                            mediaHelper.playSound(R.raw.gamestart);
                        }
                    }

                    @Override
                    public void onAnimationStart(@NonNull Animator animation) {
                        updateUI();
                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animation) {
                        countdownRemaining--;
                        updateUI();
                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {
                        binding.tvCountdown.setVisibility(View.GONE);
                        viewModel.getGame().setPause(false);
                        countdownFuture.cancel(true);
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animation) {
                        countdownFuture.cancel(true);
                    }
                });
            });
        }
    }

    // Animation
    public void animate(View view, Animator.AnimatorListener listener) {
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(1, 0);
        alphaAnimator.addListener(listener);
        alphaAnimator.setDuration(1000);
        alphaAnimator.setRepeatCount(countdown);
        alphaAnimator.addUpdateListener(val -> view.setAlpha((Float) val.getAnimatedValue()));
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1, 1.5f);
        scaleAnimator.setDuration(1000);
        scaleAnimator.setRepeatCount(countdown);
        scaleAnimator.addUpdateListener(val -> {
            view.setScaleX((Float) val.getAnimatedValue());
            view.setScaleY((Float) val.getAnimatedValue());
        });

        alphaAnimator.start();
        scaleAnimator.start();
    }
}
