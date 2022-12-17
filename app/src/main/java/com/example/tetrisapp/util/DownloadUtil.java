package com.example.tetrisapp.util;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import com.example.tetrisapp.BuildConfig;

import java.io.File;

public class DownloadUtil implements Runnable {
    Activity activity;
    String URL;
    String fileName;
    String notificationDescription = "";
    String notificationTitle = "";

    public DownloadUtil(Activity activity, String URL, String fileName, String notificationDescription, String notificationTitle) {
        this.activity = activity;
        this.URL = URL;
        this.notificationDescription = notificationDescription;
        this.notificationTitle = notificationTitle;
        this.fileName = fileName;
    }

    public DownloadUtil(Activity activity, String URL, String fileName) {
        this.activity = activity;
        this.URL = URL;
        this.fileName = fileName;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private long downloadFileToExternalStorage() {
        File target = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
        );
        if (target.exists()) target.delete();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL));

        request.setDescription(notificationDescription);
        request.setTitle(notificationTitle);
        request.allowScanningByMediaScanner();
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }

    private void installUpdate() {
        File file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
        );

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctx, Intent intent) {
                Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                install.setDataAndType(
                        FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", file),
                        getMimeType(file.getPath()));
                activity.startActivity(install);

                activity.unregisterReceiver(this);
                activity.finish();
            }
        };

        activity.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void run() {
        downloadFileToExternalStorage();
        installUpdate();
    }
}
