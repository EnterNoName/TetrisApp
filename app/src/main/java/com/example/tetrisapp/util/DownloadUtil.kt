package com.example.tetrisapp.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment

class DownloadUtil {
    private val context: Context
    private val url: String
    private val fileName: String
    private val notificationDescription: String
    private val notificationTitle: String
    private val dirPath = Environment.DIRECTORY_DOWNLOADS
    var downloadId: Long = -1L
        private set

    constructor(context: Context, url: String, fileName: String) {
        this.context = context
        this.url = url
        notificationDescription = "Downloading the update..."
        notificationTitle = "Tetris"
        this.fileName = fileName
        downloadFileToExternalStorage()
    }

    constructor(
        context: Context,
        url: String,
        fileName: String,
        notificationDescription: String,
        notificationTitle: String
    ) {
        this.context = context
        this.url = url
        this.notificationDescription = notificationDescription
        this.notificationTitle = notificationTitle
        this.fileName = fileName
        downloadFileToExternalStorage()
    }

    private fun downloadFileToExternalStorage() {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setDescription(notificationDescription)
        request.setTitle(notificationTitle)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(dirPath, fileName)
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request)
    }

    fun setOnCompleteListener(callback: (uri: Uri, type: String) -> Unit) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                callback(manager.getUriForDownloadedFile(downloadId), manager.getMimeTypeForDownloadedFile(downloadId))
                context.unregisterReceiver(this)
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun deleteDownload() {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        if (downloadId == -1L) return
        manager.remove(downloadId)
    }
}