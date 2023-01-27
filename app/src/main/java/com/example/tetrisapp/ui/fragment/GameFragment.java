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
import com.example.tetrisapp.data.remote.GameService;
import com.example.tetrisapp.databinding.FragmentGameBinding;
import com.example.tetrisapp.databinding.SidebarBinding;
import com.example.tetrisapp.databinding.SidebarMultiplayerBinding;
import com.example.tetrisapp.model.game.Piece;
import com.example.tetrisapp.model.game.Tetris;
import com.example.tetrisapp.model.local.model.PlayerGameData;
import com.example.tetrisapp.model.local.model.Tetromino;
import com.example.tetrisapp.model.local.model.UserInfo;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.ui.viewmodel.GameViewModel;
import com.example.tetrisapp.util.MediaPlayerUtil;
import com.example.tetrisapp.util.OnGestureListener;
import com.example.tetrisapp.util.OnTouchListener;
import com.example.tetrisapp.util.PusherUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.channel.PresenceChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class GameFragment extends Fragment implements Callback<DefaultPayload> {
    private static final String TAG = "GameFragment";
    private static final int MOVEMENT_INTERVAL = 125;

    private GameViewModel viewModel;
    private FragmentGameBinding binding;
    private SidebarBinding sidebarBinding = null;
    private SidebarMultiplayerBinding sidebarMultiplayerBinding = null;

    private int countdown;
    private int countdownRemaining;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> futureMoveRight = null;
    private ScheduledFuture<?> futureMoveLeft = null;
    private Future<?> countdownFuture = null;

    private MediaPlayer gameMusic;
    @Inject
    MediaPlayerUtil mediaHelper;
    @Inject
    GameService gameService;
    @Inject
    @Nullable
    Pusher pusher;

    private PresenceChannel channel;
    private boolean spectatorMode = false;
    private boolean gameEnded = false;

    SharedPreferences preferences;

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

        GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
        if (args.getCountdown() != -1) {
            countdown = args.getCountdown();
        } else {
            countdown = preferences.getInt(getString(R.string.setting_countdown), 5);
        }

        countdownRemaining = countdown;

        float volume = preferences.getInt(getString(R.string.setting_music_volume), 5) / 10f;
        gameMusic = MediaPlayer.create(requireContext(), R.raw.main);
        gameMusic.setOnPreparedListener(MediaPlayer::pause);
        gameMusic.setVolume(volume, volume);
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

        // Inflate view stub
        GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
        binding.stub.setOnInflateListener((stub, inflated) -> {
            if (args.getLobbyCode() != null) {
                sidebarMultiplayerBinding = SidebarMultiplayerBinding.bind(inflated);
                // TODO: Implement all player pause
                binding.btnPause.setVisibility(View.GONE);
            } else {
                sidebarBinding = SidebarBinding.bind(inflated);
            }
        });

        if (args.getLobbyCode() != null) {
            binding.stub.setLayoutResource(R.layout.sidebar_multiplayer);
        } else {
            binding.stub.setLayoutResource(R.layout.sidebar);
        }
        binding.stub.inflate();

        initOnClickListeners();
        initGameListeners();

        updateScoreboard();
        updatePieceViews();

        if (sidebarMultiplayerBinding != null) initMultiplayerGameView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!(sidebarMultiplayerBinding != null && !gameEnded)) {
            countdown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.getGame().setPause(true);

        // TODO: Implement all player pause
        if (sidebarMultiplayerBinding != null && !gameEnded) {
            gameService.declareLoss(new TokenPayload(viewModel.getIdToken())).enqueue(this);
        }
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (sidebarMultiplayerBinding != null && !gameEnded) {
//            gameService.declareLoss(new TokenPayload(viewModel.getIdToken())).enqueue(this);
//        }
//    }

    @SuppressLint("SetTextI18n")
    private void initMultiplayerGameView() {
        GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
        sidebarMultiplayerBinding.gameViewCompetitor.setGame(viewModel.getMockTetris());

        channel = pusher.getPresenceChannel("presence-" + args.getLobbyCode());

        PusherUtil.bindPlayerGameData(channel, data -> {
            try {
                String exclude = channel.getMe().getId();

                if (spectatorMode) {
                    exclude = updateSpectatorView(data);
                }

                updateMultiplayerSideView(data, exclude);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        });

        PusherUtil.bindGameOver(channel, data -> {
            try {
                // Getting winner's info
                UserInfo winnerUserInfo = PusherUtil.getUserInfo(channel, data.userId);
                if (winnerUserInfo == null) return;

                gameEnded = true;
                requireActivity().runOnUiThread(() -> {
                    // Setting game over action parameters
                    GameFragmentDirections.ActionGameFragmentToGameOverFragment action = GameFragmentDirections.actionGameFragmentToGameOverFragment();
                    action.setScore(viewModel.getGame().getScore());
                    action.setLevel(viewModel.getGame().getLevel());
                    action.setLines(viewModel.getGame().getLines());
                    action.setLobbyCode(args.getLobbyCode());
                    action.setWinnerUid(data.userId);
                    action.setWinnerUsername(winnerUserInfo.getName());
                    action.setPlacement(getSelfPlacement());
                    Navigation.findNavController(binding.getRoot()).navigate(action);
                });
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        });

        PusherUtil.bindPlayerLost(channel, data -> {
            try {
                if (viewModel.getUserGameDataMap().containsKey(data.userId)) {
                    Objects.requireNonNull(viewModel.getUserGameDataMap().get(data.userId)).isPlaying = false;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        });

        PusherUtil.createEventListener(
                (channelName, user) -> {},
                (channelName, user) -> {
                    if (viewModel.getUserGameDataMap().containsKey(user.getId())) {
                        Objects.requireNonNull(viewModel.getUserGameDataMap().get(user.getId())).isPlaying = false;
                    }
                },
                (message, e) -> {}
        );
    }

    @SuppressLint("SetTextI18n")
    private String updateSpectatorView(PlayerGameData data) {
        viewModel.getUserGameDataMap().put(data.userId, data);

        // Find opponent with max score
        List<PlayerGameData> userGameValues = new ArrayList<>(viewModel.getUserGameDataMap().values());
        userGameValues.sort(Comparator.comparingInt(o -> o.score));
        PlayerGameData bestScoringPlayer = !userGameValues.isEmpty() ? userGameValues.get(userGameValues.size() - 1) : null;
        if (bestScoringPlayer == null) return "";

        UserInfo bestScoringPlayerInfo = PusherUtil.getUserInfo(channel, bestScoringPlayer.userId);
        if (bestScoringPlayerInfo == null) return "";

//        PieceConfiguration configuration = PieceConfigurations.valueOf(maxScoringUserInfo.getConfiguration()).getConfiguration();

        // Init current piece
        Piece piece = viewModel.getMockTetris().getConfiguration().get(bestScoringPlayer.tetromino.name).copy();
        piece.setMatrix(bestScoringPlayer.tetromino.matrix);
        piece.setCol(bestScoringPlayer.tetromino.x);
        piece.setRow(bestScoringPlayer.tetromino.y);

        // Init current piece shadow
        Piece pieceShadow = viewModel.getMockTetris().getConfiguration().get(bestScoringPlayer.tetrominoShadow.name).copy();
        pieceShadow.setMatrix(bestScoringPlayer.tetrominoShadow.matrix);
        pieceShadow.setCol(bestScoringPlayer.tetrominoShadow.x);
        pieceShadow.setRow(bestScoringPlayer.tetrominoShadow.y);

        // Set opponent related game views
//        viewModel.getMockTetrisSpectate().setConfiguration(configuration);
        viewModel.getMockTetrisSpectate().setScore(bestScoringPlayer.score);
        viewModel.getMockTetrisSpectate().setLevel(bestScoringPlayer.level);
        viewModel.getMockTetrisSpectate().setLines(bestScoringPlayer.lines);
        viewModel.getMockPlayfieldSpectate().setState(bestScoringPlayer.playfield);
        viewModel.getMockTetrisSpectate().setShadow(pieceShadow);
        viewModel.getMockTetrisSpectate().setTetromino(piece);

        binding.gameView.postInvalidate();
        requireActivity().runOnUiThread(() -> {
            binding.tvSpectate.setText("Spectating:\n" + bestScoringPlayerInfo.getName());

            sidebarMultiplayerBinding.score.setText(bestScoringPlayer.score + "");
            sidebarMultiplayerBinding.level.setText(bestScoringPlayer.level + "");
            sidebarMultiplayerBinding.lines.setText(bestScoringPlayer.lines + "");
            sidebarMultiplayerBinding.combo.setText(bestScoringPlayer.combo + "");

            if (bestScoringPlayer.tetrominoSequence.length > 1) {
                sidebarMultiplayerBinding.pvNext1.setPiece(viewModel.getConfiguration().get(bestScoringPlayer.tetrominoSequence[0]).copy());
                sidebarMultiplayerBinding.pvNext2.setPiece(viewModel.getConfiguration().get(bestScoringPlayer.tetrominoSequence[1]).copy());
            }

            if (bestScoringPlayer.heldTetromino != null) {
                sidebarMultiplayerBinding.pvHold.setPiece(viewModel.getConfiguration().get(bestScoringPlayer.heldTetromino).copy());
            }
        });

        return bestScoringPlayer.userId;
    }

    @SuppressLint("SetTextI18n")
    private void updateMultiplayerSideView(PlayerGameData data, String exclude) {
        // Save received user game data
        viewModel.getUserGameDataMap().put(data.userId, data);

        // Find opponent with max score
        List<PlayerGameData> userGameValues = new ArrayList<>(viewModel.getUserGameDataMap().values());
        userGameValues.sort(Comparator.comparingInt(o -> o.score));

        PlayerGameData bestScoringPlayer = !userGameValues.isEmpty() ? userGameValues.get(userGameValues.size() - 1) : null;
        if (bestScoringPlayer == null) return;

        if (bestScoringPlayer.userId.equals(exclude)) {
            bestScoringPlayer = userGameValues.size() > 1 ? userGameValues.get(userGameValues.size() - 2) : null;
        }
        if (bestScoringPlayer == null) {
            hideMultiplayerSideView();
            return;
        } else {
            showMultiplayerSideView();
        }


        UserInfo bestScoringPlayerInfo = PusherUtil.getUserInfo(channel, bestScoringPlayer.userId);
        if (bestScoringPlayerInfo == null) return;

//        PieceConfiguration configuration = PieceConfigurations.valueOf(bestScoringPlayerInfo.getConfiguration()).getConfiguration();

        // Init current piece
        Piece piece = viewModel.getMockTetris().getConfiguration().get(bestScoringPlayer.tetromino.name).copy();
        piece.setMatrix(bestScoringPlayer.tetromino.matrix);
        piece.setCol(bestScoringPlayer.tetromino.x);
        piece.setRow(bestScoringPlayer.tetromino.y);

        // Init current piece shadow
        Piece pieceShadow = viewModel.getMockTetris().getConfiguration().get(bestScoringPlayer.tetrominoShadow.name).copy();
        pieceShadow.setMatrix(bestScoringPlayer.tetrominoShadow.matrix);
        pieceShadow.setCol(bestScoringPlayer.tetrominoShadow.x);
        pieceShadow.setRow(bestScoringPlayer.tetrominoShadow.y);

        // Set opponent related game views
//        viewModel.getMockTetris().setConfiguration(configuration);
        viewModel.getMockTetris().setScore(bestScoringPlayer.score);
        viewModel.getMockTetris().setLevel(bestScoringPlayer.level);
        viewModel.getMockTetris().setLines(bestScoringPlayer.lines);
        viewModel.getMockPlayfield().setState(bestScoringPlayer.playfield);
        viewModel.getMockTetris().setTetromino(piece);
        viewModel.getMockTetris().setShadow(pieceShadow);

        sidebarMultiplayerBinding.gameViewCompetitor.postInvalidate();

        PlayerGameData finalBestScoringPlayer = bestScoringPlayer;
        requireActivity().runOnUiThread(() -> {
            sidebarMultiplayerBinding.tvScoreCompetitor.setText(bestScoringPlayerInfo.getName() + "'s\nScore:");
            sidebarMultiplayerBinding.scoreCompetitor.setText(finalBestScoringPlayer.score + "");
        });
    }

    private void hideMultiplayerSideView() {
        requireActivity().runOnUiThread(() -> {
            sidebarMultiplayerBinding.gameViewCompetitor.setVisibility(View.GONE);
            sidebarMultiplayerBinding.tvScoreCompetitor.setVisibility(View.GONE);
            sidebarMultiplayerBinding.scoreCompetitor.setVisibility(View.GONE);
        });
    }

    private void showMultiplayerSideView() {
        requireActivity().runOnUiThread(() -> {
            sidebarMultiplayerBinding.gameViewCompetitor.setVisibility(View.VISIBLE);
            sidebarMultiplayerBinding.tvScoreCompetitor.setVisibility(View.VISIBLE);
            sidebarMultiplayerBinding.scoreCompetitor.setVisibility(View.VISIBLE);
        });
    }

    private void sendGameData() {
        Gson gson = new Gson();

        PlayerGameData data = new PlayerGameData(
                binding.gameView.getGame().getScore(),
                binding.gameView.getGame().getLines(),
                binding.gameView.getGame().getLevel(),
                binding.gameView.getGame().getCombo(),
                new Tetromino(
                        binding.gameView.getGame().getCurrentPiece().getName(),
                        binding.gameView.getGame().getCurrentPiece().getMatrix(),
                        binding.gameView.getGame().getCurrentPiece().getCol(),
                        binding.gameView.getGame().getCurrentPiece().getRow()
                ),
                new Tetromino(
                        binding.gameView.getGame().getShadow().getName(),
                        binding.gameView.getGame().getShadow().getMatrix(),
                        binding.gameView.getGame().getShadow().getCol(),
                        binding.gameView.getGame().getShadow().getRow()
                ),
                binding.gameView.getGame().getHeldPiece(),
                binding.gameView.getGame().getTetrominoSequence().toArray(new String[0]),
                binding.gameView.getGame().getPlayfield().getState()
        );

        try {
            channel.trigger("client-user-update-data", gson.toJson(data));
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private int getSelfPlacement() {
        List<PlayerGameData> userGameValues = new ArrayList<>(viewModel.getUserGameDataMap().values());
        userGameValues.sort(Comparator.comparingInt(o -> o.score));

        int placement = userGameValues.size();
        for (int i = 0; i < userGameValues.size(); i++) {
            if (viewModel.getGame().getScore() >= userGameValues.get(i).score) {
                placement = i + 2;
            }
        }

        return placement;
    }

    @SuppressLint("SetTextI18n")
    private void updateScoreboard() {
        requireActivity().runOnUiThread(() -> {
            if (sidebarBinding != null) {
                sidebarBinding.score.setText(viewModel.getGame().getScore() + "");
                sidebarBinding.level.setText(viewModel.getGame().getLevel() + "");
                sidebarBinding.lines.setText(viewModel.getGame().getLines() + "");
                sidebarBinding.combo.setText(viewModel.getGame().getCombo() + "");
            } else {
                sidebarMultiplayerBinding.score.setText(viewModel.getGame().getScore() + "");
                sidebarMultiplayerBinding.level.setText(viewModel.getGame().getLevel() + "");
                sidebarMultiplayerBinding.lines.setText(viewModel.getGame().getLines() + "");
                sidebarMultiplayerBinding.combo.setText(viewModel.getGame().getCombo() + "");
            }
        });

        float playbackSpeed = 2 - viewModel.getGame().getSpeed() / (float) Tetris.DEFAULT_SPEED;
        gameMusic.setPlaybackParams(new PlaybackParams().setSpeed(playbackSpeed));
    }

    private void updatePieceViews() {
        requireActivity().runOnUiThread(() -> {
            if (sidebarBinding != null) {
                sidebarBinding.pvNext1.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(0)).copy());
                sidebarBinding.pvNext2.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(1)).copy());
                sidebarBinding.pvNext3.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(2)).copy());
                sidebarBinding.pvNext4.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(3)).copy());
                if (viewModel.getGame().getHeldPiece() != null) {
                    sidebarBinding.pvHold.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getHeldPiece()).copy());
                }
            } else {
                sidebarMultiplayerBinding.pvNext1.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(0)).copy());
                sidebarMultiplayerBinding.pvNext2.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(1)).copy());
                if (viewModel.getGame().getHeldPiece() != null) {
                    sidebarMultiplayerBinding.pvHold.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getHeldPiece()).copy());
                }
            }
        });
    }

    private void initGameListeners() {
        viewModel.getGame().setOnGameValuesUpdate(this::updateScoreboard);
        viewModel.getGame().setOnHold(this::updatePieceViews);
        viewModel.getGame().setOnMove(() -> {
            if (sidebarMultiplayerBinding != null) sendGameData();
            binding.gameView.postInvalidate();
        });
        viewModel.getGame().setOnSolidify(() -> {
            mediaHelper.playSound(
                    R.raw.solidify,
                    preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
            );
            updatePieceViews();
        });
        viewModel.getGame().setOnGameOver(() -> {
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;

            mediaHelper.playSound(
                    R.raw.gameover,
                    preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
            );

            if (sidebarMultiplayerBinding != null) {
                sendGameData();
                gameService.declareLoss(new TokenPayload(viewModel.getIdToken())).enqueue(this);
                return;
            }

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
                mediaHelper.playSound(
                        R.raw.click,
                        preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
                );
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
                mediaHelper.playSound(
                        R.raw.click,
                        preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
                );
            }

            @Override
            public void onTapUp() {
                if (futureMoveRight != null) futureMoveRight.cancel(true);
            }
        });

        binding.btnRotateLeft.setOnClickListener(v -> {
            viewModel.getGame().rotateTetrominoLeft();
            mediaHelper.playSound(
                    R.raw.click,
                    preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
            );
        });

        binding.btnRotateRight.setOnClickListener(v -> {
            viewModel.getGame().rotateTetrominoRight();
            mediaHelper.playSound(
                    R.raw.click,
                    preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
            );
        });

        binding.btnDown.setOnTouchListener(new OnGestureListener(getContext()) {
            @Override
            public void onDoubleTap() {
                viewModel.getGame().hardDrop();
            }

            @Override
            public void onTapDown() {
                viewModel.getGame().setSoftDrop(true);
                mediaHelper.playSound(
                        R.raw.click,
                        preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
                );
            }

            @Override
            public void onTapUp() {
                viewModel.getGame().setSoftDrop(false);
            }
        });

        if (sidebarBinding != null) {
            sidebarBinding.cvHold.setOnClickListener(v -> {
                mediaHelper.playSound(
                        R.raw.click,
                        preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
                );
                viewModel.getGame().hold();
            });
        } else {
            sidebarMultiplayerBinding.cvHold.setOnClickListener(v -> {
                mediaHelper.playSound(
                        R.raw.click,
                        preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
                );
                viewModel.getGame().hold();
            });
        }

        binding.btnPause.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnPause.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_gameFragment_to_pauseFragment));
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

    private void countdown() {
        countdownFuture = executor.submit(new CountdownRunnable());
    }

    private void switchToSpectatorMode() {
        binding.btnLeft.setVisibility(View.GONE);
        binding.btnRight.setVisibility(View.GONE);
        binding.btnDown.setVisibility(View.GONE);
        binding.btnRotateLeft.setVisibility(View.GONE);
        binding.btnRotateRight.setVisibility(View.GONE);
        binding.btnPause.setVisibility(View.GONE);

        binding.gameView.setGame(viewModel.getMockTetrisSpectate());
        binding.tvSpectate.setVisibility(View.VISIBLE);
        spectatorMode = true;
    }

    @Override
    public void onResponse(Call<DefaultPayload> call, Response<DefaultPayload> response) {
        requireActivity().runOnUiThread(this::switchToSpectatorMode);
    }

    @Override
    public void onFailure(Call<DefaultPayload> call, Throwable t) {

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
                            mediaHelper.playSound(
                                    R.raw.countdown,
                                    preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
                            );
                        } else {
                            countdownRemaining = countdown;
                            binding.tvCountdown.setText("GO!");
                            mediaHelper.playSound(
                                    R.raw.gamestart,
                                    preferences.getInt(getString(R.string.setting_sfx_volume), 5) / 10f
                            );
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
