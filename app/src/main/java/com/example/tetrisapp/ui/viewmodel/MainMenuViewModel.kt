package com.example.tetrisapp.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.tetrisapp.util.DownloadUtil

class MainMenuViewModel: ViewModel() {
    var downloadUri: Uri? = null
    var downloadUtil: DownloadUtil? = null
    val visiblePermissionDialogQueue = mutableListOf<String>()

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeLastOrNull()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted) {
            visiblePermissionDialogQueue.add(0, permission)
        }
    }
}