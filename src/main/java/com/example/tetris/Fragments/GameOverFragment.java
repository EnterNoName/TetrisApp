package com.example.tetris.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tetris.Models.Tetris;
import com.example.tetris.R;
import com.example.tetris.Utils.JSON;

public class GameOverFragment extends Fragment implements View.OnClickListener {
    public GameOverFragment() {
        super(R.layout.fragment_game_over);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Tetris.GameStatistics stats = requireArguments().getParcelable("statistics");

        TextView tvScore = view.findViewById(R.id.currentScore);
        TextView tvHighScore = view.findViewById(R.id.highScore);

        tvScore.setText(getString(R.string.final_score, stats.getScore()));

        int highScore = JSON.writeStats(getContext(), stats);

        if (stats.getScore() > highScore) {
            tvHighScore.setText(R.string.new_high_score);
        } else {
            tvHighScore.setText(getString(R.string.current_high_score, highScore));
        }

        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        requireActivity().finish();
    }
}