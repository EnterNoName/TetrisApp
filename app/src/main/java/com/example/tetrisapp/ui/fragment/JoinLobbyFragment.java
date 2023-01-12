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
import com.example.tetrisapp.data.remote.LobbyService;
import com.example.tetrisapp.databinding.FragmentJoinLobbyBinding;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.OnTouchListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class JoinLobbyFragment extends Fragment implements Callback<DefaultPayload> {
    public static final String TAG = "JoinLobbyFragment";
    private FragmentJoinLobbyBinding binding;

    @Inject FirebaseUser firebaseUser;
    @Inject LobbyService lobbyService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentJoinLobbyBinding.inflate(inflater, container, false);
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
        binding.btnCreateLobby.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_joinLobbyFragment_to_createLobbyFragment));

        binding.btnJoinLobby.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnJoinLobby.setOnClickListener(v -> {
            if (firebaseUser == null) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_joinLobbyFragment_to_accountFragment);
                return;
            }

            String code = binding.etInviteCode.getText().toString();
            firebaseUser.getIdToken(true)
                    .addOnCompleteListener(task -> lobbyService
                        .joinLobby(new TokenPayload(task.getResult().getToken()), code)
                        .enqueue(this)
                    ).addOnFailureListener(e -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_joinLobbyFragment_to_accountFragment));
        });
    }

    @Override
    public void onResponse(@NonNull Call<DefaultPayload> call, Response<DefaultPayload> response) {
        if (response.code() == 401) {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createLobbyFragment_to_accountFragment);
        }

        if (response.code() == 200 && response.body() != null && response.body().status.equals("success")) {
            JoinLobbyFragmentDirections.ActionJoinLobbyFragmentToLobbyFragment action = JoinLobbyFragmentDirections.actionJoinLobbyFragmentToLobbyFragment();
            action.setInviteCode(response.body().message);
            Navigation.findNavController(binding.getRoot()).navigate(action);
        }
    }

    @Override
    public void onFailure(@NonNull Call<DefaultPayload> call, @NonNull Throwable t) {

    }
}
