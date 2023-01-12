package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.FragmentMultiplayerBinding;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.OnTouchListener;

public class MultiplayerFragment extends DialogFragment {
    public static final String TAG = "MultiplayerFragment";
    private FragmentMultiplayerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMultiplayerBinding.inflate(inflater, container, false);

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOnClickListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnJoinLobby.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnJoinLobby.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_multiplayerFragment_to_joinLobbyFragment));

        binding.btnCreateLobby.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_multiplayerFragment_to_createLobbyFragment));
    }
}
