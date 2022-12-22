package com.example.tetrisapp.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
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
import com.example.tetrisapp.databinding.MainMenuFragmentBinding;
import com.example.tetrisapp.model.remote.Update;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.ConnectionHelper;
import com.example.tetrisapp.util.DownloadUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.EOFException;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MainMenuFragment extends Fragment {
    private static final String TAG = "MainMenuFragment";
    private MainMenuFragmentBinding binding;
    private ConnectionHelper connectionHelper;

    @Inject UpdateService updateService;
    private Response<Update> update = null;

    // Permission handling
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (!checkInstallPackagesPermissions()) {
            showUpdatesDisabledSnackbar();
        }
    });
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!checkWriteExternalStoragePermissions()) {
            showUpdatesDisabledSnackbar();
        }
    });

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = MainMenuFragmentBinding.inflate(inflater, container, false);
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
        connectionHelper = new ConnectionHelper(requireActivity());
        initClickListeners();
        checkUpdate();
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
            installUpdate();
        } else {
            connectionHelper.checkInternetConnection(this::fetchUpdate);
        }
    }

    private void fetchUpdate() {
        updateService.getUpdate().enqueue(new Callback<Update>() {
            @Override
            public void onResponse(@NonNull Call<Update> call, @NonNull Response<Update> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (BuildConfig.VERSION_CODE < response.body().versionId) {
                        update = response;
                        showUpdateDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<Update> call, Throwable t) {
                if (!(t instanceof EOFException)) {
                    t.printStackTrace();
                }
            }
        });
    }

    private void installUpdate() {
        if (update.body() == null) return;

        String URL = update.body().url;
        Executors.newSingleThreadExecutor().submit(
                new DownloadUtil(requireActivity(), URL, getString(R.string.app_name) + ".apk")
        );
    }

    private void showUpdateDialog() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.LightDialogTheme)
                .setTitle(update.body() != null ? update.body().title : "")
                .setMessage(update.body().description)
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> {})
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> {
                    if (checkPermissions()) {
                        installUpdate();
                    } else {
                        requestPermissions();
                    }
                }).show();
    }

    private void initClickListeners() {
        binding.btnSingleplayer.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mainMenuFragment_to_gameFragment);
            ((MainActivity) requireActivity()).getClickMP().start();
            ((MainActivity) requireActivity()).getGameStartBtnMP().start();
        });
        binding.btnExit.setOnClickListener(v -> {
            requireActivity().finishAndRemoveTask();
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnLeaderboard.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mainMenuFragment_to_scoresFragment);
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnSettings.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnMultiplayer.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnSocials.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnSignIn.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mainMenuFragment_to_accountFragment);
            ((MainActivity) requireActivity()).getClickMP().start();
        });
        binding.btnRationaleContinue.setOnClickListener(v -> requestPermissions());
        binding.btnRationaleDecline.setOnClickListener(v -> {
            hideRequestPermissionRationale();
            showUpdatesDisabledSnackbar();
        });
    }

    private boolean checkPermissions() {
        return checkInstallPackagesPermissions() && checkWriteExternalStoragePermissions();
    }

    private void requestPermissions() {
        if (!checkInstallPackagesPermissions()) {
            requestInstallPackagesPermissions();
        } else if (!checkWriteExternalStoragePermissions()) {
            requestWriteExternalStoragePermissions();
        }
    }

    private boolean checkInstallPackagesPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return requireActivity().getPackageManager().canRequestPackageInstalls();
        } else {
            return true;
        }
    }

    private boolean checkWriteExternalStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestInstallPackagesPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.REQUEST_INSTALL_PACKAGES)) {
                showRequestPermissionRationale();
            } else {
                activityResultLauncher.launch(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + requireActivity().getPackageName())));
            }
        }
    }

    private void requestWriteExternalStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showRequestPermissionRationale();
            } else {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void showRequestPermissionRationale() {
        binding.permissionRationale.setVisibility(View.VISIBLE);
    }

    private void hideRequestPermissionRationale() {
        binding.permissionRationale.setVisibility(View.GONE);
    }

    private void showUpdatesDisabledSnackbar() {
        Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                .setAction(R.string.update_feature_retry, v -> requestPermissions()).show();
    }
}
