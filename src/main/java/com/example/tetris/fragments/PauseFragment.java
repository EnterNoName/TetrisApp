package com.example.tetris.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.View;

import com.example.tetris.util.JSON;
import com.example.tetris.view.Tetris;
import com.example.tetris.R;

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