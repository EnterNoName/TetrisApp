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
import com.example.tetrisapp.databinding.FragmentGameOverBinding;
import com.example.tetrisapp.model.local.entity.LeaderboardEntry;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.OnClickListener;

import java.util.Date;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class GameOverFragment extends Fragment {
    private final static String TAG = "GameOverFragment";
    private FragmentGameOverBinding binding;
    @Inject
    LeaderboardDao leaderboardDao;

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

        initClickListeners();
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
                    .subscribe(i -> {
                    }, throwable -> Log.e(TAG, throwable.getLocalizedMessage()));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initClickListeners() {
        binding.btnLeave.setOnTouchListener(new OnClickListener((MainActivity) requireActivity()));
        binding.btnLeave.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_mainMenuFragment));

        binding.btnRetry.setOnTouchListener(new OnClickListener((MainActivity) requireActivity()));
        binding.btnRetry.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_gameFragment));

        binding.btnShare.setOnTouchListener(new OnClickListener((MainActivity) requireActivity()));
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