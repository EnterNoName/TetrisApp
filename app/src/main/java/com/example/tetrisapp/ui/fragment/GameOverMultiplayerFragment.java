package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.remote.GameService;
import com.example.tetrisapp.databinding.FragmentGameOverBinding;
import com.example.tetrisapp.model.local.model.UserInfo;
import com.example.tetrisapp.model.remote.callback.SimpleCallback;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.example.tetrisapp.util.OnTouchListener;
import com.example.tetrisapp.util.PusherUtil;
import com.google.firebase.auth.FirebaseUser;
import com.pusher.client.Pusher;
import com.pusher.client.channel.PresenceChannel;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GameOverMultiplayerFragment extends GameOverFragment {
    private GameOverMultiplayerFragmentArgs args;

    @Inject
    GameService gameService;
    @Inject @Nullable
    FirebaseUser user;
    @Inject @Nullable
    Pusher pusher;

    PresenceChannel channel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameOverBinding.inflate(inflater, container, false);
        args = GameOverMultiplayerFragmentArgs.fromBundle(getArguments());
        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        PusherUtil.unbindGameStart(channel);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void updateUI() {
        binding.score.setText(args.getScore() + "");
        binding.level.setText(args.getLevel() + "");
        binding.lines.setText(args.getLines() + "");

        channel = pusher.getPresenceChannel("presence-" + args.getLobbyCode());
        UserInfo winnerUserInfo = PusherUtil.getUserInfo(channel, args.getWinnerUid());

        insertScoreInDB(args.getScore(), args.getLevel(), args.getLines());
        binding.tvHighScore.setText(args.getPlacement() == 1 ?
                "GG! You've won!" :
                String.format(
                        Locale.getDefault(),
                        "GG! You've placed №%d\nThe winner is %s",
                        args.getPlacement(),
                        winnerUserInfo.getName()
                ));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initClickListeners() {
        PusherUtil.bindGameStart(channel, data -> requireActivity().runOnUiThread(() -> {
            GameOverMultiplayerFragmentDirections.ActionGameOverMultiplayerFragmentToGameMultiplayerFragment action = GameOverMultiplayerFragmentDirections.actionGameOverMultiplayerFragmentToGameMultiplayerFragment();
            action.setLobbyCode(args.getLobbyCode());
            action.setCountdown(data.countdown);
            Navigation.findNavController(binding.getRoot()).navigate(action);
        }));

        binding.btnLeave.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnLeave.setOnClickListener(v -> {
            pusher.unsubscribe("presence-" + args.getLobbyCode());
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverMultiplayerFragment_to_mainMenuFragment);
        });

        binding.btnRetry.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnRetry.setOnClickListener(v -> FirebaseTokenUtil.getFirebaseToken(token ->
                gameService.startGame(new TokenPayload(token)).enqueue(new SimpleCallback<>())));

        binding.btnShare.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    String.format(getString(R.string.share_text),
                            args.getScore())
            );
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)));
        });
    }
}
