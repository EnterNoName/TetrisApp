package com.example.tetrisapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.MainMenuFragmentBinding;
import com.example.tetrisapp.util.JSON;
import com.example.tetrisapp.viewmodel.MainMenuViewModel;

import org.json.JSONException;
import org.json.JSONObject;

public class MainMenuFragment extends Fragment {
    private MainMenuViewModel viewModel;
    private MainMenuFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MainMenuViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.main_menu_fragment, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.setStartCallback(() -> requireActivity().getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, GameFragment.class, null)
                .commit());
        viewModel.setQuitCallback(() -> requireActivity().finishAndRemoveTask());
        viewModel.setResetScoreCallback(this::resetHighScore);

        updateHighScore();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHighScore();
    }

    private void updateHighScore() {
        int highScore = 0;
        JSONObject data = JSON.read(requireContext(), "save.json");
        try {
            if (!data.isNull("bestAttempts")) {
                highScore = data.getJSONArray("bestAttempts").getJSONObject(0).getInt("score");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        viewModel.setCurrentHighScore(highScore);
    }

    private void resetHighScore() {
        JSON.write(requireContext(), "save.json", new JSONObject(), false);
        updateHighScore();
    }
}
