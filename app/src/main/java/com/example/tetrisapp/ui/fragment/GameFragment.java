package com.example.tetrisapp.ui.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.ui.viewmodel.GameViewModel;
import com.example.tetrisapp.util.TouchListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameBinding.inflate(inflater, container, false);
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
        new MaterialAlertDialogBuilder(requireContext(), R.style.LightDialogTheme)
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
            ((MainActivity) requireActivity()).getSolidifyMP().start();
            this.updatePieceViews();
        });
        viewModel.getGame().setOnGameOver(() -> {
            ((MainActivity) requireActivity()).getMainThemeMP().pause();
            ((MainActivity) requireActivity()).getMainThemeMP().reset();
            ((MainActivity) requireActivity()).getGameOverMP().start();

            GameFragmentDirections.ActionGameFragmentToGameOverFragment action = GameFragmentDirections.actionGameFragmentToGameOverFragment();
            action.setScore(viewModel.getGame().getScore());
            action.setLevel(viewModel.getGame().getLevel());
            action.setLines(viewModel.getGame().getLines());
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
        viewModel.getGame().setOnPause(() -> ((MainActivity) requireActivity()).getMainThemeMP().pause());
        viewModel.getGame().setOnResume(() -> ((MainActivity) requireActivity()).getMainThemeMP().start());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnLeft.setOnTouchListener(new TouchListener(getContext()) {
            @Override
            public void onTapDown() {
                futureMoveLeft = executor.scheduleAtFixedRate(new MoveLeftRunnable(), 0, MOVEMENT_INTERVAL, TimeUnit.MILLISECONDS);
                ((MainActivity) requireActivity()).getClickMP().start();
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
                ((MainActivity) requireActivity()).getClickMP().start();
            }

            @Override
            public void onTapUp() {
                if (futureMoveRight != null) futureMoveRight.cancel(true);
            }
        });
        binding.btnRotateLeft.setOnClickListener(v -> {
            viewModel.getGame().rotateTetrominoLeft();
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnRotateRight.setOnClickListener(v -> {
            viewModel.getGame().rotateTetrominoRight();
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnPause.setOnClickListener(v -> {
            viewModel.getGame().setPause(true);
            Navigation.findNavController(v).navigate(R.id.action_gameFragment_to_pauseFragment);
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnDown.setOnTouchListener(new TouchListener(getContext()) {
            @Override
            public void onDoubleTap() {
                viewModel.getGame().hardDrop();
            }

            @Override
            public void onTapDown() {
                viewModel.getGame().setSoftDrop(true);
                ((MainActivity) requireActivity()).getClickMP().start();
            }

            @Override
            public void onTapUp() {
                viewModel.getGame().setSoftDrop(false);
            }
        });
        binding.include.cvHold.setOnClickListener(v -> {
            viewModel.getGame().hold();
            ((MainActivity) requireActivity()).getClickMP().start();
        });
    }

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
                    @Override
                    public void onAnimationStart(@NonNull Animator animation) {
                        if (countdownRemaining > 0) {
                            binding.tvCountdown.setText(countdownRemaining + "");
                            ((MainActivity) requireActivity()).getCountdownMP().start();
                        } else {
                            countdownRemaining = countdown;
                            binding.tvCountdown.setText("GO!");
                            ((MainActivity) requireActivity()).getGameStartMP().start();
                        }
                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {
                        binding.tvCountdown.setVisibility(View.GONE);
                        viewModel.getGame().setPause(false);
                        countdownFuture.cancel(true);
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animation) {
                        countdownRemaining--;
                        if (countdownRemaining > 0) {
                            binding.tvCountdown.setText(countdownRemaining + "");
                            ((MainActivity) requireActivity()).getCountdownMP().start();
                        } else {
                            countdownRemaining = countdown;
                            binding.tvCountdown.setText("GO!");
                            ((MainActivity) requireActivity()).getGameStartMP().start();
                        }
                    }
                });
            });
        }
    }

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
