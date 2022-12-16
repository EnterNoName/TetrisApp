package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.GameOverFragmentBinding;
import com.example.tetrisapp.room.model.LeaderboardEntry;
import com.example.tetrisapp.util.Singleton;

import java.util.Date;

public class GameOverFragment extends Fragment {
    private GameOverFragmentBinding binding;

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
        binding = GameOverFragmentBinding.inflate(inflater, container, false);
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

        AsyncTask.execute(() -> {
            int currentHighScore = 0;

            LeaderboardEntry entry = Singleton.INSTANCE.getDb().leaderboardDao().getBest();
            if (entry != null) currentHighScore = entry.score;

            int finalCurrentHighScore = currentHighScore;
            requireActivity().runOnUiThread(() -> {
                binding.tvHighScore.setText(finalCurrentHighScore >= args.getScore() ?
                        String.format(getString(R.string.current_high_score), finalCurrentHighScore) :
                        String.format(getString(R.string.new_high_score), args.getScore()));
            });
        });

        AsyncTask.execute(() -> {
            LeaderboardEntry entry = new LeaderboardEntry();
            entry.score = args.getScore();
            entry.level = args.getLevel();
            entry.lines = args.getLines();
            entry.date = new Date();

            Singleton.INSTANCE.getDb().leaderboardDao().insert(entry);
        });

        binding.btnLeave.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_mainMenuFragment);
        });
        binding.btnRetry.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_gameFragment);
        });
    }
}