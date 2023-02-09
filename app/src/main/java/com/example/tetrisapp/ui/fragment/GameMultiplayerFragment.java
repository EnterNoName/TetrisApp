package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.remote.GameService;
import com.example.tetrisapp.databinding.SidebarMultiplayerBinding;
import com.example.tetrisapp.model.game.Tetris;
import com.example.tetrisapp.model.local.model.PlayerGameData;
import com.example.tetrisapp.model.local.model.UserInfo;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.OnTouchListener;
import com.example.tetrisapp.util.PusherUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GameMultiplayerFragment extends GameFragment {
    private static final String TAG = "GameMultiplayerFragment";
    private SidebarMultiplayerBinding sidebarBinding;
    private GameMultiplayerFragmentArgs args;

    @Inject GameService gameService;
    @Inject @Nullable Pusher pusher;
    private PresenceChannel channel;
    private boolean spectating = false;

    PresenceChannelEventListener disconnectListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = GameMultiplayerFragmentArgs.fromBundle(getArguments());
        countdown = args.getCountdown();
        countdownRemaining = countdown;
    }

    @Override
    protected void inflateSidebar() {
        binding.stub.setOnInflateListener((stub, inflated) -> {
            sidebarBinding = SidebarMultiplayerBinding.bind(inflated);
            sidebarBinding.gameViewCompetitor.setGame(viewModel.getMockTetris());
        });

        binding.stub.setLayoutResource(R.layout.sidebar_multiplayer);
        binding.stub.inflate();
    }

    @Override
    public void onResume() {
        super.onResume();
        initMultiplayerGameView();
    }

    @Override
    public void onPause() {
        super.onPause();
        PusherUtil.unbindGameOver(channel);
        PusherUtil.unbindPlayerGameData(channel);
        PusherUtil.unbindPlayerLost(channel);
        PusherUtil.unbindPause(channel);
        channel.unbindGlobal(disconnectListener);
    }

    @Override
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

    @Override
    protected void updatePieceViews() {
        requireActivity().runOnUiThread(() -> {
            sidebarBinding.pvNext1.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(0)).copy());
            sidebarBinding.pvNext2.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getTetrominoSequence().get(1)).copy());
            if (viewModel.getGame().getHeldPiece() != null) {
                sidebarBinding.pvHold.setPiece(viewModel.getConfiguration().get(viewModel.getGame().getHeldPiece()).copy());
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initPauseOnClickListener() {
        binding.btnPause.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnPause.setOnClickListener(v -> {
            multiplayerPause();
            channel.trigger(PusherUtil.GAME_PAUSE, "");
        });
    }

    @Override
    protected void initGameListeners() {
        super.initGameListeners();

        viewModel.getGame().setOnMove(() -> {
            binding.gameView.postInvalidate();
            sendGameData();
        });
        viewModel.getGame().setOnGameOver(() -> {
            switchToSpectatorMode();
            declareLoss();
        });
    }

    @Override
    protected void initSidebarOnClickListeners() {
        sidebarBinding.pvHold.setOnClickListener(v -> {
            viewModel.getGame().hold();
            mediaHelper.playSound(R.raw.click, sfxVolume);
        });
    }

    @SuppressLint("SetTextI18n")
    private void initMultiplayerGameView() {
        channel = pusher.getPresenceChannel("presence-" + args.getLobbyCode());

        PusherUtil.bindPlayerGameData(channel, data -> {
            try {
                String currentPlayerUid = channel.getMe().getId();

                if (spectating) currentPlayerUid = updateSpectatorView(data);
                updateMultiplayerSideView(data, currentPlayerUid);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        });

        PusherUtil.bindPause(channel, this::multiplayerPause);

        PusherUtil.bindGameOver(channel, data -> {
            try {
                multiplayerGameOver();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        });

        PusherUtil.bindPlayerLost(channel, data -> {
            try {
                viewModel.getUserGameDataMap().put(data.userId, data);

                String currentPlayerUid = channel.getMe().getId();

                if (spectating) currentPlayerUid = updateSpectatorView(data);
                updateMultiplayerSideView(data, currentPlayerUid);

                if (viewModel.getUserGameDataMap().values().stream().anyMatch(i -> i.isPlaying) || !viewModel.getGame().isGameOver()) return;
                multiplayerGameOver();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        });

        disconnectListener = PusherUtil.createEventListener(
                (channelName, user) -> {},
                (channelName, user) -> {
                    PlayerGameData data = viewModel.getUserGameDataMap().getOrDefault(user.getId(), null);
                    if (data == null) return;

                    int placement = viewModel.getUserGameDataMap().values().stream()
                            .reduce(0, (x, y) -> x + (y.isPlaying ? 1 : 0), Integer::sum) +
                            (viewModel.getGame().isGameOver() ? 1 : 0) + 1  ;
                    viewModel.getUserGameDataMap().put(user.getId(), new PlayerGameData(data, false, placement));

                    if (viewModel.getUserGameDataMap().values().stream().anyMatch(i -> i.isPlaying) || !viewModel.getGame().isGameOver()) return;
                    multiplayerGameOver();
                },
                (message, e) -> {}
        );

        channel.bindGlobal(disconnectListener);
    }

    private void declareLoss() {
        try {
            Gson gson = new Gson();
            channel.trigger(
                    PusherUtil.PLAYER_DECLARE_LOSS,
                    gson.toJson(viewModel.getGameData(channel.getMe().getId()))
            );
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private void sendGameData() {
        try {
            Gson gson = new Gson();
            channel.trigger(
                    PusherUtil.PLAYER_UPDATE_DATA,
                    gson.toJson(viewModel.getGameData(channel.getMe().getId()))
            );
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private void multiplayerPause() {
        requireActivity().runOnUiThread(() -> {
            GameMultiplayerFragmentDirections.ActionGameMultiplayerFragmentToPauseFragment action = GameMultiplayerFragmentDirections.actionGameMultiplayerFragmentToPauseFragment();
            action.setLobbyCode(args.getLobbyCode());
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
    }

    private void multiplayerGameOver() {
        // Getting winner's info
        UserInfo winnerUserInfo;
        winnerUserInfo = PusherUtil.getUserInfo(channel, viewModel.getWinnerUid());
        if (winnerUserInfo == null) return;

        requireActivity().runOnUiThread(() -> {
            // Setting game over action parameters
            GameMultiplayerFragmentDirections.ActionGameMultiplayerFragmentToGameOverFragment action = GameMultiplayerFragmentDirections.actionGameMultiplayerFragmentToGameOverFragment();
            action.setScore(viewModel.getGame().getScore());
            action.setLevel(viewModel.getGame().getLevel());
            action.setLines(viewModel.getGame().getLines());
            action.setLobbyCode(args.getLobbyCode());
            action.setWinnerUid(winnerUserInfo.getUid());
            action.setWinnerUsername(winnerUserInfo.getName());
            action.setPlacement(viewModel.getPlacement());
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
    }

    @SuppressLint("SetTextI18n")
    private String updateSpectatorView(PlayerGameData data) {
        viewModel.getUserGameDataMap().put(data.userId, data);

        String spectatedPlayerUid = viewModel.updateSpectatorMockTetris();
        PlayerGameData spectatedPlayerData = viewModel.getUserGameDataMap().getOrDefault(spectatedPlayerUid, null);
        if (spectatedPlayerData == null) return null;

        UserInfo spectatedPlayerInfo = PusherUtil.getUserInfo(channel, spectatedPlayerUid);
        if (spectatedPlayerInfo == null) return null;

        binding.gameView.postInvalidate();
        requireActivity().runOnUiThread(() -> {
            binding.tvSpectate.setText("Spectating:\n" + spectatedPlayerInfo.getName());

            sidebarBinding.score.setText(spectatedPlayerData.score + "");
            sidebarBinding.level.setText(spectatedPlayerData.level + "");
            sidebarBinding.lines.setText(spectatedPlayerData.lines + "");
            sidebarBinding.combo.setText(spectatedPlayerData.combo + "");

            if (spectatedPlayerData.tetrominoSequence.length >= 2) {
                sidebarBinding.pvNext1.setPiece(viewModel.getMockTetrisSpectate().getConfiguration().get(spectatedPlayerData.tetrominoSequence[0]).copy());
                sidebarBinding.pvNext2.setPiece(viewModel.getMockTetrisSpectate().getConfiguration().get(spectatedPlayerData.tetrominoSequence[1]).copy());
            }

            if (spectatedPlayerData.heldTetromino != null) {
                sidebarBinding.pvHold.setPiece(viewModel.getMockTetrisSpectate().getConfiguration().get(spectatedPlayerData.heldTetromino).copy());
            } else {
                sidebarBinding.pvHold.setPiece(null);
            }
        });

        return spectatedPlayerUid;
    }

    @SuppressLint("SetTextI18n")
    private void updateMultiplayerSideView(PlayerGameData data, String currentPlayerUid) {
        // Save received user game data
        viewModel.getUserGameDataMap().put(data.userId, data);

        String bestScoringPlayerUid = viewModel.updateMockTetris(currentPlayerUid);
        PlayerGameData bestScoringPlayer = viewModel.getUserGameDataMap().getOrDefault(bestScoringPlayerUid, null);

        if (bestScoringPlayer == null) {
            hideCompetitorSideView();
            return;
        }

        showCompetitorSideView();


        UserInfo bestScoringPlayerInfo = PusherUtil.getUserInfo(channel, bestScoringPlayer.userId);
        if (bestScoringPlayerInfo == null) return;

        sidebarBinding.gameViewCompetitor.postInvalidate();

        requireActivity().runOnUiThread(() -> {
            sidebarBinding.tvScoreCompetitor.setText(bestScoringPlayerInfo.getName() + "'s\nScore:");
            sidebarBinding.scoreCompetitor.setText(bestScoringPlayer.score + "");
        });
    }

    private void hideCompetitorSideView() {
        requireActivity().runOnUiThread(() -> {
            sidebarBinding.gameViewCompetitor.setVisibility(View.GONE);
            sidebarBinding.tvScoreCompetitor.setVisibility(View.GONE);
            sidebarBinding.scoreCompetitor.setVisibility(View.GONE);
        });
    }

    private void showCompetitorSideView() {
        requireActivity().runOnUiThread(() -> {
            sidebarBinding.gameViewCompetitor.setVisibility(View.VISIBLE);
            sidebarBinding.tvScoreCompetitor.setVisibility(View.VISIBLE);
            sidebarBinding.scoreCompetitor.setVisibility(View.VISIBLE);
        });
    }

    private void switchToSpectatorMode() {
        requireActivity().runOnUiThread(() -> {
            binding.controls.setVisibility(View.GONE);
            binding.btnPause.setVisibility(View.GONE);

            sidebarBinding.cvHold.setClickable(false);

            binding.gameView.setGame(viewModel.getMockTetrisSpectate());
            binding.tvSpectate.setVisibility(View.VISIBLE);
            spectating = true;
        });
    }

    @Override
    protected void confirmExit() {
        viewModel.getGame().setPause(true);
        new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.exit_dialog_title))
                .setMessage(getString(R.string.exit_dialog_description))
                .setOnDismissListener((dialog) -> viewModel.getGame().setPause(false))
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> viewModel.getGame().setPause(false))
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameMultiplayerFragment_to_mainMenuFragment))
                .show();
    }
}
