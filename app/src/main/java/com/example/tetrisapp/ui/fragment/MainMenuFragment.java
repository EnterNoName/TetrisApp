package com.example.tetrisapp.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tetrisapp.BuildConfig;
import com.example.tetrisapp.R;
import com.example.tetrisapp.data.service.UpdateService;
import com.example.tetrisapp.databinding.MainMenuFragmentBinding;
import com.example.tetrisapp.model.Update;
import com.example.tetrisapp.util.ConnectionHelper;
import com.example.tetrisapp.util.PermissionHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainMenuFragment extends Fragment {
    private MainMenuFragmentBinding binding;
    private boolean hasWriteStoragePermission = false;
    private boolean hasInstallPackagesPermission = false;
    private PermissionHelper permissionHelper;
    private ConnectionHelper connectionHelper;
    private Retrofit retrofit;
    UpdateService updateService;
    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                requestPermissions();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = MainMenuFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        permissionHelper = new PermissionHelper(requireActivity(), this);
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
                () -> {
                        updateService.getUpdate().enqueue(new Callback<Update>() {
                            @Override
                            public void onResponse(Call<Update> call, Response<Update> response) {
                                if (response.isSuccessful()) {
                                    assert response.body() != null;
                                    if (BuildConfig.VERSION_NAME.compareTo(response.body().version) < 0) {
                                        new MaterialAlertDialogBuilder(requireContext(), R.style.LightDialogTheme)
                                                .setTitle(response.body().title)
                                                .setMessage(response.body().description)
                                                .setNegativeButton(getString(R.string.disagree), (dialog, which) -> {

                                                })
                                                .setPositiveButton(getString(R.string.agree), (dialog, which) -> {
                                                    requestPermissions();
                                                    if (hasWriteStoragePermission && hasInstallPackagesPermission) {
                                                        installUpdate(response.body().url);
                                                    }
                                                })
                                                .show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Update> call, Throwable t) {
                                try {
                                    throw t;
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                },
                () -> {
                    // TODO
                }
        );
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initClickListeners() {
        binding.btnSingleplayer.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_mainMenuFragment_to_gameFragment));
        binding.btnExit.setOnClickListener(v -> requireActivity().finishAndRemoveTask());
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionHelper.checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    () -> {hasWriteStoragePermission = true;},
                    () -> {
                        permissionHelper.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            () -> {hasWriteStoragePermission = true;},
                            () -> {
                            Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.update_feature_retry, v -> {requestPermissions();}).show();
                        });
                        },
                    () -> {}
            );
        } else {
            hasWriteStoragePermission = true;
        }

        permissionHelper.checkPermission(
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                () -> {hasInstallPackagesPermission = true;},
                () -> {
                    permissionHelper.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            () -> {hasInstallPackagesPermission = true;},
                            () -> {
                                Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.update_feature_retry, v -> {requestPermissions();}).show();
                            });
                },
                () -> {activityResultLauncher.launch(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));}
        );
    }

    private long downloadFileToExternalStorage(String url, String fileName, String description, String title) {
        File target = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
        );
        if (target.exists()) target.delete();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        request.setDescription(description);
        request.setTitle(title);
        request.allowScanningByMediaScanner();
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }

    private void installUpdate(String url) {
        String fileName = getString(R.string.app_name) + ".apk";
        File file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
        );

        final long downloadId = downloadFileToExternalStorage(url, fileName, getString(R.string.desc_download_notification), getString(R.string.app_name));
        DownloadManager manager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctx, Intent intent) {
                Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                install.setDataAndType(
                        FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file),
                        manager.getMimeTypeForDownloadedFile(downloadId));
                startActivity(install);

                requireActivity().unregisterReceiver(this);
                requireActivity().finish();
            }
        };

        requireActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
