package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.remote.LobbyService;
import com.example.tetrisapp.databinding.FragmentLobbyBinding;
import com.example.tetrisapp.model.local.model.UserInfo;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.ui.adapters.UsersRecyclerViewAdapter;
import com.example.tetrisapp.util.OnTouchListener;
import com.example.tetrisapp.util.PusherUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LobbyFragment extends Fragment implements Callback<DefaultPayload> {
    public static final String TAG = "LobbyFragment";
    private FragmentLobbyBinding binding;
    private boolean exitedLobby = false;

    private FirebaseAuth mAuth;
    @Inject
    LobbyService lobbyService;

    private Pusher pusher;
    private PresenceChannel channel;
    private List<UserInfo> userList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void confirmExit() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.lobby_exit_alert_title))
                .setMessage(getString(R.string.lobby_exit_alert_message))
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> {})
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> exitLobby())
                .show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLobbyBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();

        binding.list.setAdapter(new UsersRecyclerViewAdapter(this.userList));

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LobbyFragmentArgs args = LobbyFragmentArgs.fromBundle(getArguments());
        PusherUtil.unsubscribePresence();

        mAuth.getCurrentUser()
                .getIdToken(false)
                .addOnCompleteListener(task -> lobbyService
                        .exitLobby(new TokenPayload(task.getResult().getToken()))
                        .enqueue(this));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOnClickListeners();
        initPusherChannelListeners();
        updateUI();
    }

    private void initPusherChannelListeners() {
        LobbyFragmentArgs args = LobbyFragmentArgs.fromBundle(getArguments());

        mAuth.getCurrentUser()
                .getIdToken(true)
                .addOnCompleteListener(task -> {
                    pusher = PusherUtil.getPusherInstance(task.getResult().getToken(), getString(R.string.update_url) + "auth");
                    channel = PusherUtil.getPresenceChannel(pusher,"presence-" + args.getInviteCode(), new PresenceChannelEventListener() {
                        @Override
                        @SuppressLint("NotifyDataSetChanged")
                        public void onUsersInformationReceived(String channelName, Set<User> users) {
                            insertUsers(users);
                        }

                        @Override
                        public void userSubscribed(String channelName, User user) {
                            addUserToLobbyUserList(user);
                        }

                        @Override
                        public void userUnsubscribed(String channelName, User user) {
                            removeUserFromLobbyUserList(user);
                        }

                        @Override
                        public void onAuthenticationFailure(String message, Exception e) {
                        }

                        @Override
                        public void onSubscriptionSucceeded(String channelName) {
                        }

                        @Override
                        public void onEvent(PusherEvent event) {
                        }
                    });
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnExitLobby.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnExitLobby.setOnClickListener(v -> exitLobby());
    }

    private void updateUI() {
        LobbyFragmentArgs args = LobbyFragmentArgs.fromBundle(getArguments());
        binding.inviteCode.setText(args.getInviteCode());
    }

    @Override
    public void onResponse(Call<DefaultPayload> call, Response<DefaultPayload> response) {
    }

    @Override
    public void onFailure(Call<DefaultPayload> call, Throwable t) {
    }

    private void exitLobby() {
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_lobbyFragment_to_mainMenuFragment);
    }

    private void removeUserFromLobbyUserList(User user) {
        requireActivity().runOnUiThread(() -> {
            for (int i = 0; i < userList.size(); i++) {
                UserInfo userInfo = userList.get(i);
                if (!userInfo.getUid().equals(user.getId())) continue;
                userList.remove(i);
                binding.list.getAdapter().notifyItemRemoved(i);
            }
        });
    }

    private void addUserToLobbyUserList(User user) {
       Gson gson = new Gson();
       UserInfo userPresenceData = gson.fromJson(user.getInfo(), UserInfo.class);
       userPresenceData.setUid(user.getId());

       requireActivity().runOnUiThread(() -> {
           userList.add(userPresenceData);
           binding.list.getAdapter().notifyItemInserted(userList.size() - 1);
       });
    }

    private void insertUsers(Set<User> users) {
        Gson gson = new Gson();
        userList.clear();

        users.forEach(user -> {
            UserInfo userPresenceData = gson.fromJson(user.getInfo(), UserInfo.class);
            userPresenceData.setUid(user.getId());
            requireActivity().runOnUiThread(() -> userList.add(userPresenceData));
        });

        requireActivity().runOnUiThread(() -> binding.list.getAdapter().notifyDataSetChanged());
    }
}