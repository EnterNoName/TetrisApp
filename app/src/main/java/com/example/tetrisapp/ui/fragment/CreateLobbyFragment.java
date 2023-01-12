package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.remote.LobbyService;
import com.example.tetrisapp.databinding.FragmentCreateLobbyBinding;
import com.example.tetrisapp.model.remote.request.CreateLobbyPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.example.tetrisapp.util.OnTouchListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CreateLobbyFragment extends Fragment implements Callback<DefaultPayload> {
    private static final String TAG = "CreateLobbyFragment";
    private FragmentCreateLobbyBinding binding;

    @Inject
    FirebaseUser firebaseUser;
    @Inject
    LobbyService lobbyService;

    private Call<DefaultPayload> apiCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateLobbyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOnClickListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (apiCall != null) apiCall.cancel();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnBack.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).popBackStack());

        binding.btnResetSettings.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnResetSettings.setOnClickListener(v -> {
            binding.countdownSlider.setValue(3.0f);
            binding.playerLimitSlider.setValue(2.0f);
            binding.switchEnablePause.setChecked(false);
        });

        binding.btnCreateLobby.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnCreateLobby.setOnClickListener(v -> {
            if (firebaseUser == null) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createLobbyFragment_to_accountFragment);
                return;
            }

            int countdown = Math.round(binding.countdownSlider.getValue());
            int playerLimit = Math.round(binding.playerLimitSlider.getValue());
            boolean enablePause = binding.switchEnablePause.isChecked();

            FirebaseTokenUtil.getFirebaseToken(idToken -> {
                apiCall = lobbyService.createLobby(new CreateLobbyPayload(idToken, countdown, playerLimit));
                apiCall.enqueue(this);
            });
        });
    }

    @Override
    public void onResponse(@NonNull Call<DefaultPayload> call, Response<DefaultPayload> response) {
        if (response.code() == 401) {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createLobbyFragment_to_accountFragment);
        }

        if (response.isSuccessful() && response.body() != null && response.body().status.equals("success")) {
            CreateLobbyFragmentDirections.ActionCreateLobbyFragmentToLobbyFragment action = CreateLobbyFragmentDirections.actionCreateLobbyFragmentToLobbyFragment();
            action.setInviteCode(response.body().message);
            Navigation.findNavController(binding.getRoot()).navigate(action);
        }
    }

    @Override
    public void onFailure(@NonNull Call<DefaultPayload> call, @NonNull Throwable t) {
        Snackbar.make(binding.getRoot(), "Something went wrong. Try again later.", Snackbar.LENGTH_LONG).show();
    }
}
