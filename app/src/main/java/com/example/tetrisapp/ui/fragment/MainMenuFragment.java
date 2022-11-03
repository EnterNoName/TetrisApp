package com.example.tetrisapp.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tetrisapp.BuildConfig;
import com.example.tetrisapp.R;
import com.example.tetrisapp.databinding.MainMenuFragmentBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class MainMenuFragment extends Fragment {
    private MainMenuFragmentBinding binding;
    private boolean hasWriteStoragePermission = false;
    private boolean hasInstallPackagesPermission = false;
    private boolean hasInternetConnection = false;
    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    hasWriteStoragePermission = true;
                } else {
                    Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                            .setAction(R.string.update_feature_retry, v -> {requestPermissions();}).show();
                }
            });
    @RequiresApi(api = Build.VERSION_CODES.O)
    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK &&
                        requireActivity().getPackageManager().canRequestPackageInstalls()) {
                    hasInstallPackagesPermission = true;
                } else {
                    Snackbar.make(binding.getRoot(), R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
                            .setAction(R.string.update_feature_retry, v -> {requestPermissions();}).show();
                }
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
        initClickListeners();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initClickListeners() {
        binding.btnSingleplayer.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_mainMenuFragment_to_gameFragment));
        binding.btnExit.setOnClickListener(v -> requireActivity().finishAndRemoveTask());
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasWriteStoragePermission = true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                hasWriteStoragePermission = true;
            } else if (requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                binding.permissionRationale.setVisibility(View.VISIBLE);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                hasWriteStoragePermission = true;
            } else if (requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                binding.permissionRationale.setVisibility(View.VISIBLE);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (requireActivity().getPackageManager().canRequestPackageInstalls()) {
                hasWriteStoragePermission = true;
            } else if (requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.REQUEST_INSTALL_PACKAGES)) {
                activityResultLauncher.launch(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
            } else {
                activityResultLauncher.launch(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
            }
        } else {
            hasWriteStoragePermission = true;
        }
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

    private void installUpdate() {
        String url = getString(R.string.update_link);
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
