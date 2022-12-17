package com.example.tetrisapp.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class PermissionHelper {
    private final Activity activity;
    private final ActivityResultCaller resultCaller;

    public PermissionHelper(Activity activity, ActivityResultCaller resultCaller) {
        this.activity = activity;
        this.resultCaller = resultCaller;
    }

    public void checkPermission(String permission, Callback onGranted, Callback onDeclined, Callback onShouldShowRequestRationale) {
        if (Objects.equals(permission, Manifest.permission.REQUEST_INSTALL_PACKAGES)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (activity.getPackageManager().canRequestPackageInstalls()) {
                    onGranted.call();
                } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                    onShouldShowRequestRationale.call();
                } else {
                    onDeclined.call();
                }
            } else {
                onGranted.call();
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    activity.getBaseContext(), permission) ==
                    PackageManager.PERMISSION_GRANTED) {
                onGranted.call();
            } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                onShouldShowRequestRationale.call();
            } else {
                onDeclined.call();
            }
        }
    }

    public void requestPermission(String permission, Callback onGranted, Callback onDeclined) {
        if (Objects.equals(permission, Manifest.permission.REQUEST_INSTALL_PACKAGES)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ActivityResultLauncher<Intent> activityResultLauncher =
                        resultCaller.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                            if (result.getResultCode() == Activity.RESULT_OK &&
                                    activity.getPackageManager().canRequestPackageInstalls()) {
                                onGranted.call();
                            } else {
                                onDeclined.call();
                            }
                        });
                activityResultLauncher.launch(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
            }
        } else {
            ActivityResultLauncher<String> requestPermissionLauncher =
                    resultCaller.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            onGranted.call();
                        } else {
                            onDeclined.call();
                        }
                    });
            requestPermissionLauncher.launch(permission);
        }
    }

    public interface Callback {
        void call();
    }
}
