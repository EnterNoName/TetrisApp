package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
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

        binding.btnLeave.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_mainMenuFragment);
        });
        binding.btnRetry.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gameOverFragment_to_gameFragment);
        });
    }
}