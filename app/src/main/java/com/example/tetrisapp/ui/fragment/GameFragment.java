package com.example.tetrisapp.ui.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.example.tetrisapp.databinding.SidebarBinding;
import com.example.tetrisapp.model.game.Tetris;
import com.example.tetrisapp.model.game.configuration.PieceConfigurations;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.ui.viewmodel.GameViewModel;
import com.example.tetrisapp.util.MediaPlayerUtil;
import com.example.tetrisapp.util.OnGestureListener;
import com.example.tetrisapp.util.OnTouchListener;
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
    private static final int MOVEMENT_INTERVAL = 125;

    protected GameViewModel viewModel;
    protected FragmentGameBinding binding;
    private SidebarBinding sidebarBinding;

    protected int countdown;
    protected int countdownRemaining;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> futureMoveRight = null;
    private ScheduledFuture<?> futureMoveLeft = null;
    private Future<?> countdownFuture = null;

    protected MediaPlayer gameMusic;
    @Inject
    MediaPlayerUtil mediaHelper;

    protected SharedPreferences preferences;

    protected float musicVolume = 0.5f;
    protected float sfxVolume = 0.5f;

    private final OnBackPressedCallback backPressCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            confirmExit();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        viewModel.setConfiguration(PieceConfigurations
                .valueOf(preferences.getString(getString(R.string.setting_configuration), "DEFAULT"))
                .getConfiguration()
        );
        viewModel.setGame(new Tetris(
                viewModel.getConfiguration(),
                viewModel.getConfiguration().getStarterPieces(),
                viewModel.getConfiguration().getInitialHistory()
        ));

        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressCallback);

        countdown = preferences.getInt(getString(R.string.setting_countdown), 5);
        countdownRemaining = countdown;

        musicVolume = preferences.getInt(getString(R.string.setting_music_volume), 5) / 10f;
        sfxVolume = preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f;

        gameMusic = MediaPlayer.create(requireContext(), R.raw.main);
        gameMusic.setOnPreparedListener(MediaPlayer::pause);
        gameMusic.setVolume(musicVolume, musicVolume);
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

        inflateSidebar();

        if (!preferences.getBoolean(getString(R.string.setting_control_scheme), false)) {
            initOnClickListeners();
        } else {
            initOnTouchListeners();
        }

        initGameListeners();
        updateScoreboard();
        updatePieceViews();
    }

    protected void inflateSidebar() {
        binding.stub.setOnInflateListener((stub, inflated) -> {
            sidebarBinding = SidebarBinding.bind(inflated);
        });

        binding.stub.setLayoutResource(R.layout.sidebar);
        binding.stub.inflate();
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;
        }

        viewModel.getGame().stop();
    }

    @SuppressLint("SetTextI18n")
    protected void updateScoreboard() {
        requireActivity().runOnUiThread(() -> {
            sidebarBinding.score.setText(viewModel.getGame().getScore() + "");
            sidebarBinding.level.setText(viewModel.getGame().getLevel() + "");
            sidebarBinding.lines.setText(viewModel.getGame().getLines() + "");
            sidebarBinding.combo.setText(viewModel.getGame().getCombo() + "");
        });

        float playbackSpeed = 2 - viewModel.getGame().getSpeed() / (float) Tetris.DEFAULT_SPEED;
        gameMusic.setPlaybackParams(new PlaybackParams().setSpeed(playbackSpeed));
    }

    protected void updatePieceViews() {
        requireActivity().runOnUiThread(() -> {
            sidebarBinding.pvNext1.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(0)).copy());
            sidebarBinding.pvNext2.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(1)).copy());
            sidebarBinding.pvNext3.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(2)).copy());
            sidebarBinding.pvNext4.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(3)).copy());
            if (viewModel.getGame().getHeldPiece() != null) {
                sidebarBinding.pvHold.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getHeldPiece()).copy());
            }
        });
    }

    protected void initGameListeners() {
        viewModel.getGame().setOnGameValuesUpdate(this::updateScoreboard);
        viewModel.getGame().setOnHold(this::updatePieceViews);
        viewModel.getGame().setOnMove(() -> binding.gameView.postInvalidate());
        viewModel.getGame().setOnSolidify(() -> {
            mediaHelper.playSound(R.raw.solidify, sfxVolume);
            updatePieceViews();
        });
        viewModel.getGame().setOnGameOver(this::onGameOver);
        viewModel.getGame().setOnPause(() -> gameMusic.pause());
        viewModel.getGame().setOnResume(() -> gameMusic.start());
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initOnTouchListeners() {
        binding.gameView.post(() -> binding.gameView.setOnTouchListener(new View.OnTouchListener() {
            private final int width = binding.gameView.getWidth();
            private final int blockSize = width / 10;
            private float x = -1;
            private int col = -1;
            private boolean moved = false;

            private static final int SWIPE_DISTANCE_THRESHOLD = 200;
            private static final int SWIPE_VELOCITY_THRESHOLD = 400;
            private final GestureDetector gestureDetector = new GestureDetector(requireContext(), new OnGestureListener.GestureListener() {
                @Override
                public boolean onSwipe(Direction direction, float distance, float velocity) {
                    if (distance >= SWIPE_DISTANCE_THRESHOLD && velocity >= SWIPE_VELOCITY_THRESHOLD) {
                        switch (direction) {
                            case up:
                                viewModel.getGame().hold();
                                break;
                            case down:
                                viewModel.getGame().setSoftDrop(true);
                                break;
                        }
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    viewModel.getGame().hardDrop();
                    return true;
                }
            });

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                int index = event.getActionIndex();
                float x = event.getX(index);

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        this.col = viewModel.getGame().getCurrentPiece().getCol();
                        this.x = x;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float xDiff = Math.abs(this.x - x);

                        if (xDiff > blockSize / 2f) this.moved = true;

                        if (this.moved) {
                            int col = viewModel.getGame().getCurrentPiece().getCol();
                            int colDiff = Math.round((this.x - x) / this.blockSize);
                            int desiredCol = this.col - colDiff;

                            if (desiredCol == col) return true;
                            while (desiredCol != col) {
                                if (desiredCol > col) {
                                    viewModel.getGame().moveTetrominoRight();
                                } else {
                                    viewModel.getGame().moveTetrominoLeft();
                                }

                                if (col == viewModel.getGame().getCurrentPiece().getCol()) break;
                                col = viewModel.getGame().getCurrentPiece().getCol();
                            }

                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (!this.moved) {
                            if (x > (width / 3f) * 2) {
                                viewModel.getGame().rotateTetrominoRight();
                                return true;
                            } else if (x < width / 3f) {
                                viewModel.getGame().rotateTetrominoLeft();
                                return true;
                            }
                        }

                        this.moved = false;
                        break;
                }

                gestureDetector.onTouchEvent(event);
                return true;
            }
        }));
        initPauseOnClickListener();
        initSidebarOnClickListeners();
        binding.controls.setVisibility(View.GONE);
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initOnClickListeners() {
        binding.btnLeft.setOnTouchListener(new OnGestureListener(getContext()) {
            @Override
            public void onTapDown() {
                futureMoveLeft = executor.scheduleAtFixedRate(new MoveLeftRunnable(), 0, MOVEMENT_INTERVAL, TimeUnit.MILLISECONDS);
                mediaHelper.playSound(R.raw.click, sfxVolume);
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
                mediaHelper.playSound(R.raw.click, sfxVolume);
            }

            @Override
            public void onTapUp() {
                if (futureMoveRight != null) futureMoveRight.cancel(true);
            }
        });

        binding.btnRotateLeft.setOnClickListener(v -> {
            viewModel.getGame().rotateTetrominoLeft();
            mediaHelper.playSound(R.raw.click, sfxVolume);
        });

        binding.btnRotateRight.setOnClickListener(v -> {
            viewModel.getGame().rotateTetrominoRight();
            mediaHelper.playSound(R.raw.click, sfxVolume);
        });

        binding.btnDown.setOnTouchListener(new OnGestureListener(getContext()) {
            @Override
            public void onDoubleTap() {
                viewModel.getGame().hardDrop();
            }

            @Override
            public void onTapDown() {
                viewModel.getGame().setSoftDrop(true);
                mediaHelper.playSound(R.raw.click, sfxVolume);
            }

            @Override
            public void onTapUp() {
                viewModel.getGame().setSoftDrop(false);
            }
        });

        initPauseOnClickListener();
        initSidebarOnClickListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initPauseOnClickListener() {
        binding.btnPause.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnPause.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_gameFragment_to_pauseFragment));
    }

    protected void initSidebarOnClickListeners() {
        sidebarBinding.cvHold.setOnClickListener(v -> {
            viewModel.getGame().hold();
            mediaHelper.playSound(R.raw.click, sfxVolume);
        });
    }

    protected void confirmExit() {
        viewModel.getGame().setPause(true);
        new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.exit_dialog_title))
                .setMessage(getString(R.string.exit_dialog_description))
                .setOnDismissListener((dialog) -> viewModel.getGame().setPause(false))
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> viewModel.getGame().setPause(false))
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameFragment_to_mainMenuFragment))
                .show();
    }

    private void countdown() {
        countdownFuture = executor.submit(new CountdownRunnable());
    }

    protected void onGameOver() {
        viewModel.getGame().stop();

        gameMusic.stop();
        gameMusic.release();
        gameMusic = null;

        mediaHelper.playSound(R.raw.gameover, sfxVolume);

        GameFragmentDirections.ActionGameFragmentToGameOverFragment action = GameFragmentDirections.actionGameFragmentToGameOverFragment();
        action.setScore(viewModel.getGame().getScore());
        action.setLevel(viewModel.getGame().getLevel());
        action.setLines(viewModel.getGame().getLines());
        Navigation.findNavController(binding.getRoot()).navigate(action);
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
            float volume = preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f;

            requireActivity().runOnUiThread(() -> {
                binding.tvCountdown.setVisibility(View.VISIBLE);
                animate(binding.tvCountdown, new Animator.AnimatorListener() {
                    public void updateUI() {
                        if (countdownRemaining > 0) {
                            binding.tvCountdown.setText(countdownRemaining + "");
                            mediaHelper.playSound(R.raw.countdown, volume);
                        } else {
                            countdownRemaining = countdown;
                            binding.tvCountdown.setText("GO!");
                            mediaHelper.playSound(R.raw.gamestart, volume);
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
