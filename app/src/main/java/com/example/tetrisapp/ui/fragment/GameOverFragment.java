package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.room.rxjava3.EmptyResultSetException;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.local.dao.LeaderboardDao;
import com.example.tetrisapp.data.remote.GameService;
import com.example.tetrisapp.data.remote.LeaderboardService;
import com.example.tetrisapp.databinding.FragmentGameOverBinding;
import com.example.tetrisapp.model.local.entity.LeaderboardEntry;
import com.example.tetrisapp.model.local.model.GameStartedData;
import com.example.tetrisapp.model.remote.callback.SimpleCallback;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.DateTimeUtil;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.example.tetrisapp.util.HashUtil;
import com.example.tetrisapp.util.LeaderboardUtil;
import com.example.tetrisapp.util.OnTouchListener;
import com.example.tetrisapp.util.PusherUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.channel.PresenceChannel;

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class GameOverFragment extends Fragment{
    private final static String TAG = "GameOverFragment";
    private FragmentGameOverBinding binding;
    private GameOverFragmentArgs args;

    @Inject LeaderboardDao leaderboardDao;
    @Inject LeaderboardService leaderboardService;
    @Inject GameService gameService;
    @Inject @Nullable FirebaseUser user;
    @Inject @Nullable Pusher pusher;

    PresenceChannel channel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_mainMenuFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameOverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        args = GameOverFragmentArgs.fromBundle(getArguments());

        binding.score.setText(args.getScore() + "");
        binding.level.setText(args.getLevel() + "");
        binding.lines.setText(args.getLines() + "");

        if  (args.getLobbyCode() == null) {
            handleGameOverSingleplayer();
            initClickListeners();
        } else {
            channel = pusher.getPresenceChannel("presence-" + args.getLobbyCode());
            handleGameOverMultiplayer();
            initClickListenersMultiplayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if  (args.getLobbyCode() != null) {
            PusherUtil.unbindGameStart(channel);
        }
    }

    private void handleGameOverMultiplayer() {
        insertScoreInDB();

        binding.tvHighScore.setText(args.getPlacement() == 1 ?
                "GG! You've won!" :
                String.format(Locale.getDefault(),"GG! You've placed №%d\nThe winner is %s", args.getPlacement(), args.getWinnerUsername()));
    }

    private void handleGameOverSingleplayer() {
        leaderboardDao.getBest()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(leaderboardEntry -> {
                    int currentHighScore = leaderboardEntry.score;

                    binding.tvHighScore.setText(currentHighScore >= args.getScore() ?
                            String.format(getString(R.string.current_high_score), currentHighScore) :
                            String.format(getString(R.string.new_high_score), args.getScore()));

                    insertScoreInDB();
                }, throwable -> {
                    if (throwable instanceof EmptyResultSetException) {
                        binding.tvHighScore.setText(0 >= args.getScore() ?
                                String.format(getString(R.string.current_high_score), 0) :
                                String.format(getString(R.string.new_high_score), args.getScore()));

                        insertScoreInDB();
                    } else {
                        Log.e(TAG, throwable.getLocalizedMessage());
                    }
                });
    }

    private void insertScoreInDB() {
        if (args.getScore() < 1)  return;

        LeaderboardEntry entry = new LeaderboardEntry();
        entry.score = args.getScore();
        entry.level = args.getLevel();
        entry.lines = args.getLines();
        entry.date = new Date();

        FirebaseTokenUtil.getFirebaseToken(token -> {
            LeaderboardUtil leaderboardUtil = new LeaderboardUtil(token, leaderboardDao, leaderboardService);
            leaderboardUtil.insert(entry);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initClickListeners() {
        binding.btnLeave.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnLeave.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_mainMenuFragment));

        binding.btnRetry.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnRetry.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_gameFragment));

        binding.btnShare.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    String.format(getString(R.string.share_text),
                            GameOverFragmentArgs.fromBundle(getArguments()).getScore())
            );
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)));
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initClickListenersMultiplayer() {
        PusherUtil.bindGameStart(channel, data -> requireActivity().runOnUiThread(() -> {
            GameOverFragmentDirections.ActionGameOverFragmentToGameMultiplayerFragment action = GameOverFragmentDirections.actionGameOverFragmentToGameMultiplayerFragment();
            action.setLobbyCode(args.getLobbyCode());
            action.setCountdown(data.countdown);
            Navigation.findNavController(binding.getRoot()).navigate(action);
        }));

        binding.btnLeave.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnLeave.setOnClickListener(v -> {
            pusher.unsubscribe("presence-" + args.getLobbyCode());
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_mainMenuFragment);
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
                            GameOverFragmentArgs.fromBundle(getArguments()).getScore())
            );
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)));
        });
    }
}