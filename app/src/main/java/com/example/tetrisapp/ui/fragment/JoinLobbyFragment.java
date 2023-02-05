package com.example.tetrisapp.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.remote.LobbyService;
import com.example.tetrisapp.databinding.FragmentJoinLobbyBinding;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class JoinLobbyFragment extends DialogFragment implements Callback<DefaultPayload> {
    public static final String TAG = "JoinLobbyDialogFragment";
    private FragmentJoinLobbyBinding binding;

    private String token = null;
    @Inject LobbyService lobbyService;

    private Call<DefaultPayload> apiCall;
    private String inviteCode;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentJoinLobbyBinding.inflate(inflater, container, false);

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
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_joinLobbyFragment_to_accountFragment);
        }

        initOnClickListeners();

        JoinLobbyFragmentArgs args = JoinLobbyFragmentArgs.fromBundle(getArguments());
        if (args.getInviteCode() != null) {
            binding.etInviteCode.setText(args.getInviteCode());
            binding.btnEnter.performClick();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseTokenUtil.getFirebaseToken(token -> this.token = token);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (apiCall != null) apiCall.cancel();
    }

    private void initOnClickListeners() {
        binding.btnEnter.setOnClickListener(v -> {
            binding.btnEnter.setEnabled(false);
            binding.etInviteCode.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);

            inviteCode = binding.etInviteCode.getText().toString();
            binding.etInviteCode.setText("");

            apiCall = lobbyService.joinLobby(new TokenPayload(token), inviteCode);
            apiCall.enqueue(this);
        });
    }

    // Retrofit callbacks
    @Override
    public void onResponse(@NonNull Call<DefaultPayload> call, Response<DefaultPayload> response) {
        if (call.isCanceled()) return;

        requireActivity().runOnUiThread(() -> {
            binding.btnEnter.setEnabled(true);
            binding.etInviteCode.setEnabled(true);
            binding.progressBar.setVisibility(View.GONE);
        });

        if (response.code() == 401) { // Not authorized
            NavHostFragment.findNavController(this).navigate(R.id.action_joinLobbyFragment_to_accountFragment);
        }

        if (response.code() == 400) {
            getDialog().dismiss();
            Snackbar.make(getParentFragment().requireView(), "This lobby does not exist.", Snackbar.LENGTH_LONG).show();
        }

        if (response.isSuccessful() && response.body() != null && response.body().status.equals("success")) {
            JoinLobbyFragmentDirections.ActionJoinLobbyFragmentToLobbyFragment action = JoinLobbyFragmentDirections.actionJoinLobbyFragmentToLobbyFragment();
            action.setInviteCode(inviteCode);
            NavHostFragment.findNavController(this).navigate(action);
        }
    }

    @Override
    public void onFailure(@NonNull Call<DefaultPayload> call, @NonNull Throwable t) {
        if (call.isCanceled()) return;

        requireActivity().runOnUiThread(() -> {
            binding.btnEnter.setEnabled(true);
            binding.etInviteCode.setEnabled(true);
            binding.progressBar.setVisibility(View.GONE);
        });

        getDialog().dismiss();
        Snackbar.make(binding.getRoot(), "Something went wrong. Try again later.", Snackbar.LENGTH_LONG).show();
    }
}
