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
import com.example.tetrisapp.util.OnTouchListener;
import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CreateLobbyFragment extends Fragment implements Callback<DefaultPayload> {
    private static final String TAG = "CreateLobbyFragment";
    private FragmentCreateLobbyBinding binding;

    private FirebaseAuth mAuth;
    @Inject
    LobbyService lobbyService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateLobbyBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOnClickListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnCreateLobby.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnCreateLobby.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createLobbyFragment_to_accountFragment);
                return;
            }

            mAuth.getCurrentUser()
                    .getIdToken(true)
                    .addOnCompleteListener(task -> lobbyService
                            .createLobby(new CreateLobbyPayload(task.getResult().getToken(), 5))
                            .enqueue(this));
        });
    }

    @Override
    public void onResponse(Call<DefaultPayload> call, Response<DefaultPayload> response) {
        Log.d(TAG, response.code() + " " + (response.body() != null ? response.body().message : ""));

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
    public void onFailure(Call<DefaultPayload> call, Throwable t) {
        Log.e(TAG, t.getLocalizedMessage());
    }
}
