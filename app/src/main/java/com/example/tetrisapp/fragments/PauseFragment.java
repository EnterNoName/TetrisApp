package com.example.tetrisapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.View;

import com.example.tetrisapp.util.JSON;
import com.example.tetrisapp.view.Tetris;
import com.example.tetrisapp.R;

public class PauseFragment extends Fragment {
    public PauseFragment() {
        super(R.layout.pause_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Tetris.GameStatistics stats = requireArguments().getParcelable("statistics");

        view.findViewById(R.id.resumeBtn).setOnClickListener(v -> {
            requireParentFragment().getChildFragmentManager().beginTransaction().remove(PauseFragment.this).commit();
            ((GameFragment) requireParentFragment()).onGameResume();
        });
        view.findViewById(R.id.exitActivityBtn).setOnClickListener(v -> {
            ((GameFragment) requireParentFragment()).onStopGame();
            JSON.writeStats(getContext(), stats);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, MainMenuFragment.class, null)
                    .commit();
        });
        super.onViewCreated(view, savedInstanceState);
    }
}