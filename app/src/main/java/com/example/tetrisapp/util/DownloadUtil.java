package com.example.tetrisapp.util;

import android.app.DownloadManager;
import android.app.RecoverableSecurityException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.content.FileProvider;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.tetrisapp.BuildConfig;
import com.example.tetrisapp.interfaces.DownloadCallback;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadUtil {
    private final Context context;
    private final String URL;
    private final String fileName;
    private final String notificationDescription;
    private final String notificationTitle;
    private final String dirPath = Environment.DIRECTORY_DOWNLOADS;

    private long downloadId;

    public DownloadUtil(Context context, String URL, String fileName) {
        this.context = context;
        this.URL = URL;
        this.notificationDescription = "Downloading the update...";
        this.notificationTitle = "Tetris";
        this.fileName = fileName;

        downloadFileToExternalStorage();
    }

    public DownloadUtil(Context context, String URL, String fileName, String notificationDescription, String notificationTitle) {
        this.context = context;
        this.URL = URL;
        this.notificationDescription = notificationDescription;
        this.notificationTitle = notificationTitle;
        this.fileName = fileName;

        downloadFileToExternalStorage();
    }

    private void downloadFileToExternalStorage() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL));

        request.setDescription(notificationDescription);
        request.setTitle(notificationTitle);
        request.allowScanningByMediaScanner();
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(dirPath, fileName);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.downloadId = manager.enqueue(request);
    }

    public void setOnCompleteListener(DownloadCallback callback) {
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctx, Intent intent) {
                callback.call(manager.getUriForDownloadedFile(downloadId));
                context.unregisterReceiver(this);
            }
        };

        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void deleteDownload() {
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        manager.remove(this.downloadId);
    }

    public long getDownloadId() {
        return downloadId;
    }
}
