package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.FragmentPauseBinding;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.OnTouchListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PauseFragment extends Fragment {
    private FragmentPauseBinding binding;
    private boolean dialogOpen = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPauseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initOnClickListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnResume.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnResume.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        binding.btnLeave.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnLeave.setOnClickListener(v -> confirmExit());
    }

    private void confirmExit() {
        if (!dialogOpen) {
            dialogOpen = true;
            new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                    .setTitle(getString(R.string.exit_dialog_title))
                    .setMessage(getString(R.string.exit_dialog_description))
                    .setNegativeButton(getString(R.string.disagree), (dialog, which) -> {
                        dialogOpen = false;
                    })
                    .setPositiveButton(getString(R.string.agree), (dialog, which) -> {
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_pauseFragment_to_mainMenuFragment);
                    })
                    .show();
        }
    }
}