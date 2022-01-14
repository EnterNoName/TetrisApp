package com.example.tetris.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tetris.Activities.GameActivity;
import com.example.tetris.R;

public class PauseFragment extends Fragment {
    public PauseFragment() {
        super(R.layout.fragment_pause);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        GameActivity activity = (GameActivity) requireActivity();
        view.findViewById(R.id.resumeBtn).setOnClickListener(v -> {
            activity.game.resumeThreads();
            activity.getSupportFragmentManager().beginTransaction().remove(PauseFragment.this).commit();
        });
        view.findViewById(R.id.exitActivityBtn).setOnClickListener(v -> {
            activity.finish();
        });
        super.onViewCreated(view, savedInstanceState);
    }
}