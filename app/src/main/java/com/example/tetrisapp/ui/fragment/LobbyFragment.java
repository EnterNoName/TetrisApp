package com.example.tetrisapp.ui.fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.example.tetrisapp.data.remote.GameService;
import com.example.tetrisapp.data.remote.LobbyService;
import com.example.tetrisapp.databinding.FragmentLobbyBinding;
import com.example.tetrisapp.model.local.model.GameStartedData;
import com.example.tetrisapp.model.local.model.UserInfo;
import com.example.tetrisapp.model.remote.request.TokenPayload;
import com.example.tetrisapp.model.remote.response.DefaultPayload;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.ui.adapters.UsersRecyclerViewAdapter;
import com.example.tetrisapp.ui.viewmodel.LobbyViewModel;
import com.example.tetrisapp.util.FirebaseTokenUtil;
import com.example.tetrisapp.util.OnTouchListener;
import com.example.tetrisapp.util.PusherUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.User;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LobbyFragment extends Fragment implements Callback<DefaultPayload> {
    public static final String TAG = "LobbyFragment";
    private FragmentLobbyBinding binding;
    private LobbyViewModel viewModel;

    @Inject
    LobbyService lobbyService;
    @Inject
    GameService gameService;
    @Inject
    @Nullable
    Pusher pusher;

    private PresenceChannelEventListener listener;
    private PresenceChannelEventListener listenerGameStart;
    private PresenceChannel channel;

    private boolean succeded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LobbyViewModel.class);

        // Handle back button press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        // Retrieve firebase token
        FirebaseTokenUtil.getFirebaseToken(token -> viewModel.setIdToken(token));
    }

    private void confirmExit() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.lobby_exit_alert_title))
                .setMessage(getString(R.string.lobby_exit_alert_message))
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> {
                })
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> exitLobby())
                .show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLobbyBinding.inflate(inflater, container, false);
        binding.list.setAdapter(new UsersRecyclerViewAdapter(viewModel.getUserList()));
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (succeded) {
            channel.unbind("game-started", listenerGameStart);
            channel.unbindGlobal(listener);
            return;
        }
        ;

        LobbyFragmentArgs args = LobbyFragmentArgs.fromBundle(getArguments());
        pusher.unsubscribe("presence-" + args.getInviteCode());

        lobbyService.exitLobby(new TokenPayload(viewModel.getIdToken())).enqueue(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOnClickListeners();
        initPusherChannelListeners();
        updateUI();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListeners() {
        binding.btnExitLobby.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnExitLobby.setOnClickListener(v -> exitLobby());

        binding.btnStartGame.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnStartGame.setOnClickListener(v -> gameService.startGame(new TokenPayload(viewModel.getIdToken())).enqueue(this));

        // Copies invite code to clip board
        binding.btnActionMenu.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnActionMenu.setOnClickListener(v -> showMenu(v, R.menu.menu_invite_code_action));

        binding.btnBack.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnBack.setOnClickListener(v -> exitLobby());
    }

    @SuppressLint({"NonConstantResourceId", "RestrictedApi"})
    private void showMenu(View v, int resId) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenuInflater().inflate(resId, popup.getMenu());

        MenuBuilder menuBuilder = (MenuBuilder) popup.getMenu();
        menuBuilder.setOptionalIconsVisible(true);
        for (MenuItem item : menuBuilder.getVisibleItems()) {
            int iconMarginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
            if (item.getIcon() != null) {
                item.setIcon(new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0));
            }
        }

        popup.setOnMenuItemClickListener(item -> {
            LobbyFragmentArgs args = LobbyFragmentArgs.fromBundle(getArguments());

            switch (item.getItemId()) {
                case R.id.copy_code:
                    ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(getString(R.string.invite_code), args.getInviteCode());
                    clipboard.setPrimaryClip(clip);

                    Snackbar.make(binding.getRoot(), "Copied!", Snackbar.LENGTH_SHORT).show();
                    return true;
                case R.id.share_code:
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_invite_code_subject));
                    shareIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            String.format(getString(R.string.invite_url), args.getInviteCode())
                    );
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)));
                    return true;
            }
            return true;
        });

        popup.show();
    }

    private void initPusherChannelListeners() {
        LobbyFragmentArgs args = LobbyFragmentArgs.fromBundle(getArguments());

        listener = PusherUtil.createEventListener(
                (channelName, user) -> {
                    try {
                        addUserToLobbyUserList(user);
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                },
                (channelName, user) -> {
                    try {
                        removeUserFromLobbyUserList(user);
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                },
                (message, e) -> exitLobby()
        );

        listenerGameStart = PusherUtil.createEventListener(event -> requireActivity().runOnUiThread(() -> {
            succeded = true;
            Gson gson = new Gson();
            GameStartedData data = gson.fromJson(event.getData(), GameStartedData.class);

            LobbyFragmentDirections.ActionLobbyFragmentToGameFragment action = LobbyFragmentDirections.actionLobbyFragmentToGameFragment();
            action.setLobbyCode(args.getInviteCode());
            action.setCountdown(data.countdown);
            Navigation.findNavController(binding.getRoot()).navigate(action);
        }));

        channel = PusherUtil.getPresenceChannel(
                pusher,
                "presence-" + args.getInviteCode(),
                listener);

        channel.bind("game-started", listenerGameStart);
    }

    private void updateUI() {
        LobbyFragmentArgs args = LobbyFragmentArgs.fromBundle(getArguments());
        binding.inviteCode.setText(args.getInviteCode());
        binding.tvLobbyTitle.setText(
                viewModel.getLobbyOwnerName() != null ?
                        viewModel.getLobbyOwnerName() + "'s Lobby" :
                        "Lobby"
        );
    }

    private void exitLobby() {
        Navigation.findNavController(binding.getRoot()).popBackStack();
    }

    // Remove user from user list and recycler view adapter
    private void removeUserFromLobbyUserList(User user) {
        if (binding.list.getAdapter() == null) return;

        requireActivity().runOnUiThread(() -> {
            for (int i = 0; i < viewModel.getUserList().size(); i++) {
                UserInfo userInfo = viewModel.getUserList().get(i);
                if (!userInfo.getUid().equals(user.getId())) continue;
                viewModel.getUserList().remove(i);
                binding.list.getAdapter().notifyItemRemoved(i);
                break;
            }
        });
    }

    // Get user data from json and append to user list and recycler view adapter
    private void addUserToLobbyUserList(User user) {
        if (binding.list.getAdapter() == null) return;

        Gson gson = new Gson();
        UserInfo userPresenceData = gson.fromJson(user.getInfo(), UserInfo.class);
        userPresenceData.setUid(user.getId());

        requireActivity().runOnUiThread(() -> {
            viewModel.getUserList().add(userPresenceData);
            binding.list.getAdapter().notifyItemInserted(viewModel.getUserList().size() - 1);
        });
    }

    @Override
    public void onResponse(@NonNull Call<DefaultPayload> call, @NonNull Response<DefaultPayload> response) {
        if (response.code() == 400) {
            Snackbar.make(binding.getRoot(), "Not enough players to start the game.", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFailure(@NonNull Call<DefaultPayload> call, Throwable t) {
        Log.e(TAG, t.getLocalizedMessage());
        exitLobby();
    }
}