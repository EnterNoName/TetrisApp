package com.example.tetrisapp.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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

import com.example.tetrisapp.BuildConfig;
import com.example.tetrisapp.R;
import com.example.tetrisapp.data.service.UpdateService;
import com.example.tetrisapp.databinding.MainMenuFragmentBinding;
import com.example.tetrisapp.model.Update;
import com.example.tetrisapp.ui.activity.MainActivity;
import com.example.tetrisapp.util.ConnectionHelper;
import com.example.tetrisapp.util.DownloadUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.EOFException;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainMenuFragment extends Fragment {
    private MainMenuFragmentBinding binding;
    private ConnectionHelper connectionHelper;

    private Retrofit retrofit;
    private UpdateService updateService;

    private Response<Update> update = null;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (!checkInstallPackagesPermissions()) {
            Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                    .setAction(R.string.update_feature_retry, v -> requestPermissions()).show();
        } else {
            requestPermissions();
        }
    });;
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        requestPermissions();
    });;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = MainMenuFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectionHelper = new ConnectionHelper(requireActivity());
        retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.update_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        updateService = retrofit.create(UpdateService.class);
        initClickListeners();
        checkUpdate();
    }

    private void checkUpdate() {
        connectionHelper.checkInternetConnection(
                () -> updateService.getUpdate().enqueue(new Callback<Update>() {
                    @Override
                    public void onResponse(Call<Update> call, Response<Update> response) {
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
                }),
                () -> {
                }
        );
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
                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> {
                })
                .setPositiveButton(getString(R.string.agree), (dialog, which) -> {
                    requestPermissions();
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
            ((MainActivity) requireActivity()).getClickMP().start();
        });
    }

    private boolean checkPermissions() {
        return checkInstallPackagesPermissions() && checkWriteExternalStoragePermissions();
    }

    private void requestPermissions() {
        if (checkInstallPackagesPermissions() && checkWriteExternalStoragePermissions()) {
            installUpdate();
            return;
        }

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

    private void requestInstallPackagesPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activityResultLauncher.launch(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + requireActivity().getPackageName())));
        }
    }

    private boolean checkWriteExternalStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestWriteExternalStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                        .setAction(R.string.update_feature_retry, v -> {
                            activityResultLauncher.launch(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + requireActivity().getPackageName())));
                        }).show();
            } else {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }
}
