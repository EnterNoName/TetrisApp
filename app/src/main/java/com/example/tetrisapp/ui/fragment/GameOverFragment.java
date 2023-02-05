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
import com.example.tetrisapp.databinding.FragmentGameOverBinding;
import com.example.tetrisapp.model.local.entity.LeaderboardEntry;
import com.example.tetrisapp.model.local.model.GameStartedData;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.example.tetrisapp.util.OnTouchListener;
import com.example.tetrisapp.util.PusherUtil;
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
public class GameOverFragment extends Fragment implements Callback<DefaultPayload> {
    private final static String TAG = "GameOverFragment";
    private FragmentGameOverBinding binding;

    @Inject
    LeaderboardDao leaderboardDao;
    @Inject
    GameService gameService;
    @Inject
    @Nullable
    FirebaseUser user;
    @Inject
    @Nullable
    Pusher pusher;

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
        GameOverFragmentArgs args = GameOverFragmentArgs.fromBundle(getArguments());

        binding.score.setText(args.getScore() + "");
        binding.level.setText(args.getLevel() + "");
        binding.lines.setText(args.getLines() + "");

        if  (args.getLobbyCode() == null) {
            handleGameOverSingleplayer();
            initClickListeners();
        } else {
            handleGameOverMultiplayer();
            initClickListenersMultiplayer();
        }
    }

    private void handleGameOverMultiplayer() {
        GameOverFragmentArgs args = GameOverFragmentArgs.fromBundle(getArguments());

        binding.tvHighScore.setText(args.getPlacement() == 1 ?
                "GG! You've won!" :
                String.format(Locale.getDefault(),"GG! You've placed №%d\nThe winner is %s", args.getPlacement(), args.getWinnerUsername()));
    }

    private void handleGameOverSingleplayer() {
        GameOverFragmentArgs args = GameOverFragmentArgs.fromBundle(getArguments());

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
                        Log.e("GameOverFragment", throwable.getLocalizedMessage());
                    }
                });
    }

    private void insertScoreInDB() {
        GameOverFragmentArgs args = GameOverFragmentArgs.fromBundle(getArguments());

        if (args.getScore() > 0) {
            LeaderboardEntry entry = new LeaderboardEntry();
            entry.score = args.getScore();
            entry.level = args.getLevel();
            entry.lines = args.getLines();
            entry.date = new Date();

            leaderboardDao.insert(entry)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            i -> {},
                            throwable -> Log.e(TAG, throwable.getLocalizedMessage())
                    );
        }
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
        GameOverFragmentArgs args = GameOverFragmentArgs.fromBundle(getArguments());

        PresenceChannel channel = pusher.getPresenceChannel("presence-" + args.getLobbyCode());

        PusherUtil.bindPresenceChannel(channel, "game-started", event -> requireActivity().runOnUiThread(() -> {
            Gson gson = new Gson();
            GameStartedData data = gson.fromJson(event.getData(), GameStartedData.class);

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
        binding.btnRetry.setOnClickListener(v -> FirebaseTokenUtil.getFirebaseToken(token -> {
            gameService.startGame(new TokenPayload(token)).enqueue(this);
        }));

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

    @Override
    public void onResponse(Call<DefaultPayload> call, Response<DefaultPayload> response) {

    }

    @Override
    public void onFailure(Call<DefaultPayload> call, Throwable t) {

    }
}