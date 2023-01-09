package com.example.tetrisapp.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.tetrisapp.BuildConfig;
import com.example.tetrisapp.R;
import com.example.tetrisapp.data.remote.UpdateService;
import com.example.tetrisapp.databinding.FragmentMainMenuBinding;
import com.example.tetrisapp.model.remote.response.UpdatePayload;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.ConnectionUtil;
import com.example.tetrisapp.util.DownloadUtil;
import com.example.tetrisapp.util.MimeTypeUtil;
import com.example.tetrisapp.util.OnTouchListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Response;

@AndroidEntryPoint
public class MainMenuFragment extends Fragment {
    private static final String TAG = "MainMenuFragment";
    private FragmentMainMenuBinding binding;
    private ConnectionUtil connectionHelper;

    @Inject
    UpdateService updateService;
    private Response<UpdatePayload> update = null;

    private final ActivityResultLauncher<Intent> activitySettingsResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (checkWriteExternalStoragePermissions()) {
            //  Permission granted
            installUpdate();
        } else {
            // Permission denied
            requestWriteExternalStoragePermissions();
        }
    });
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!checkWriteExternalStoragePermissions()) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Showing explanation rationale
                showRequestPermissionRationale(
                        () -> showUpdatesDisabledSnackbar(this::requestWriteExternalStoragePermissions),
                        this::requestWriteExternalStoragePermissions
                );
            } else {
                // Never ask
                showUpdatesDisabledSnackbar(() ->
                        activitySettingsResultLauncher.launch(
                                new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + requireActivity().getPackageName()))
                        ));
            }
        } else {
            //  Permission granted
            installUpdate();
        }
    });
    private final ActivityResultLauncher<IntentSenderRequest> activityIntentSenderLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Snackbar.make(binding.getRoot(), "Tetris.apk deleted successfully", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(binding.getRoot(), "Couldn't delete Tetris.apk", Snackbar.LENGTH_LONG).show();
        }
    });

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainMenuBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectionHelper = new ConnectionUtil(requireActivity());
        initClickListeners();

        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        boolean autoUpdateEnabled = preferences.getBoolean(getString(R.string.setting_auto_update), true);
        if (autoUpdateEnabled) {
            checkUpdate();
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().error(R.drawable.ic_round_account_circle_24).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        binding.ivUser.setImageTintList(null);
                        return false;
                    }
                }).into(binding.ivUser);
            }
            if (user.getDisplayName() != null) {
                binding.tvProfileName.setText(user.getDisplayName());
            }
        }
    }

    private void checkUpdate() {
        if (update != null) {
            showUpdateDialog();
        } else {
            connectionHelper.checkInternetConnection(this::fetchUpdate);
        }
    }

    private void fetchUpdate() {
        updateService.getUpdate().enqueue(new retrofit2.Callback<UpdatePayload>() {
            @Override
            public void onResponse(@NonNull Call<UpdatePayload> call, @NonNull Response<UpdatePayload> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (BuildConfig.VERSION_CODE < response.body().versionId) {
                        update = response;
                        showUpdateDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdatePayload> call, Throwable t) {
                if (!(t instanceof EOFException)) {
                    t.printStackTrace();
                }
            }
        });
    }

    private void installUpdate() {
        if (update.body() == null) return;

        String URL = update.body().url;

        deleteIfFileExits(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + getString(R.string.app_name) + ".apk");
        DownloadUtil downloadUtil = new DownloadUtil(requireContext(), URL, getString(R.string.app_name) + ".apk");
        downloadUtil.setOnCompleteListener((uri) -> {
            Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(uri, MimeTypeUtil.getMimeType(uri.toString()));
            requireContext().startActivity(install);
        });
    }

    private void showUpdateDialog() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(update.body() != null ? update.body().title : "")
                .setMessage(update.body().description)
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> {
                })
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> {
                    if (checkPermissions()) {
                        installUpdate();
                    } else {
                        requestPermissions();
                    }
                }).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initClickListeners() {
        binding.btnSingleplayer.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()).setSound(R.raw.gamestartbtn));
        binding.btnSingleplayer.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mainMenuFragment_to_gameFragment));

        binding.btnMultiplayer.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnMultiplayer.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mainMenuFragment_to_joinLobbyFragment));

        binding.btnSettings.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnSettings.setOnClickListener(v -> new SettingsFragment().show(requireActivity().getSupportFragmentManager(), SettingsFragment.TAG));

        binding.btnExit.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnExit.setOnClickListener(v -> requireActivity().finishAndRemoveTask());

        binding.btnLeaderboard.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnLeaderboard.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mainMenuFragment_to_scoresFragment));

        binding.btnSocials.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnSocials.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url)));
            startActivity(browserIntent);
        });

        binding.btnSignIn.setOnTouchListener(new OnTouchListener((MainActivity) requireActivity()));
        binding.btnSignIn.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mainMenuFragment_to_accountFragment));
    }

    private boolean checkPermissions() {
        return checkWriteExternalStoragePermissions();
    }

    private void requestPermissions() {
        if (!checkWriteExternalStoragePermissions()) {
            requestWriteExternalStoragePermissions();
        }
    }

    private boolean checkWriteExternalStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestWriteExternalStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void showRequestPermissionRationale(Callback callbackOnDecline, Callback callbackOnContinue) {
        binding.permissionRationale.setVisibility(View.VISIBLE);
        binding.btnRationaleContinue.setOnClickListener(v -> {
            hideRequestPermissionRationale();
            callbackOnContinue.call();
        });
        binding.btnRationaleDecline.setOnClickListener(v -> {
            hideRequestPermissionRationale();
            callbackOnDecline.call();
        });
    }

    private void hideRequestPermissionRationale() {
        binding.permissionRationale.setVisibility(View.GONE);
    }

    private void showUpdatesDisabledSnackbar(Callback callback) {
        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.setting_auto_update), false);
        editor.apply();

        Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                .setAction(R.string.update_feature_retry, v -> {
                    editor.putBoolean(getString(R.string.setting_auto_update), true);
                    editor.apply();
                    callback.call();
                }).show();
    }

    private void deleteIfFileExits(String filePath) {
        MediaScannerConnection.scanFile(requireContext(), new String[]{filePath}, null, (path, uri) -> {
            try {
                requireContext().getContentResolver().delete(uri, null, null);
            } catch (SecurityException e) {
                IntentSender intentSender = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Collection<Uri> uris = new ArrayList<>();
                    uris.add(uri);
                    intentSender = MediaStore.createDeleteRequest(requireContext().getContentResolver(), uris).getIntentSender();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    intentSender = ((RecoverableSecurityException) e).getUserAction().getActionIntent().getIntentSender();
                }

                if (intentSender == null) return;
                activityIntentSenderLauncher.launch(new IntentSenderRequest.Builder(intentSender).build());
            }
        });
    }

    private interface Callback {
        void call();
    }
}
