package com.example.tetrisapp.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tetrisapp.databinding.GameOverFragmentBinding;
import com.example.tetrisapp.view.Tetris;
import com.example.tetrisapp.R;
import com.example.tetrisapp.util.JSON;
import com.example.tetrisapp.viewmodel.GameOverViewModel;

public class GameOverFragment extends Fragment implements View.OnClickListener {
    private GameOverViewModel viewModel;
    private GameOverFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(GameOverViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.game_over_fragment, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Tetris.GameStatistics stats = requireArguments().getParcelable("statistics");

        int score = stats.getScore(), highScore = JSON.writeStats(getContext(), stats);
        viewModel.setScore(score);
        viewModel.setHighScore(highScore);
        viewModel.setIsNewHighScore(score > highScore);

        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ((GameFragment) requireParentFragment()).onStopGame();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, MainMenuFragment.class, null)
                .commit();
    }
}