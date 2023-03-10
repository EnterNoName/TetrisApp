package com.example.tetrisapp.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.tetrisapp.BuildConfig
import com.example.tetrisapp.R
import com.example.tetrisapp.data.local.dao.LeaderboardDao
import com.example.tetrisapp.databinding.FragmentMainMenuBinding
import com.example.tetrisapp.model.remote.response.DefaultPayload
import com.example.tetrisapp.model.remote.response.UpdatePayload
import com.example.tetrisapp.ui.activity.MainActivity
import com.example.tetrisapp.ui.viewmodel.MainMenuViewModel
import com.example.tetrisapp.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MainMenuFragment : Fragment() {
    private lateinit var binding: FragmentMainMenuBinding
    private val viewModel by viewModels<MainMenuViewModel>()
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null

    @Inject lateinit var leaderboardDao: LeaderboardDao

    private var call: HttpResponse? = null
    private var update: DefaultPayload<UpdatePayload>? = null

    private lateinit var dialogQueue: MutableList<String>

    private val storagePermissionResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            isGranted
        )
        goThroughDialogQueue()
    }

    private val multiplePermissionResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        perms.forEach { permission ->
            viewModel.onPermissionResult(
                permission.key,
                permission.value
            )
        }
        goThroughDialogQueue()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        dialogQueue = viewModel.visiblePermissionDialogQueue
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        updateUI(firebaseUser)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()

        val preferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val autoUpdateEnabled = preferences.getBoolean(getString(R.string.setting_auto_update), true)

        if (autoUpdateEnabled) {
            checkForUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        uploadScores()

        FirebaseTokenUtil.getFirebaseToken { token ->
            val activity: MainActivity = requireActivity() as MainActivity
            val preferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
            val configuration = preferences.getString(getString(R.string.setting_configuration), "DEFAULT")

            if (token != null && activity.pusher == null) {
                activity.pusher = PusherUtil.createPusherInstance(
                    getString(R.string.api_url) + "auth",
                    configuration,
                    token
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.downloadUtil?.deleteDownload()
    }

    private fun uploadScores() {
        val connectionUtil = ConnectionUtil(requireContext())
        connectionUtil.onAvailableCallback = {
            FirebaseTokenUtil.getFirebaseToken { token: String? ->
                LeaderboardUtil(
                    token,
                    getString(R.string.api_url),
                    leaderboardDao
                ).synchronise()
            }
        }
        connectionUtil.checkInternetConnection()
    }

    private fun updateUI(user: FirebaseUser?) {
        binding.ivUser.scaleType = ImageView.ScaleType.FIT_CENTER
        user?.let { u ->
            binding.tvProfileName.text = u.displayName
            u.photoUrl?.let { photoUri ->
                binding.ivUser.load(photoUri) {
                    placeholder(R.drawable.ic_round_account_circle_24)
                    error(R.drawable.ic_round_account_circle_24)
                    transformations(CircleCropTransformation())
                    crossfade(true)
                    target(onSuccess = {
                        binding.ivUser.imageTintList = null
                        binding.ivUser.setImageDrawable(it)
                    })
                }
            }
        }
    }

    private fun checkForUpdates() {
        if (viewModel.downloadUri != null) {
            updateApp(viewModel.downloadUri!!)
            return
        }

        if ((viewModel.downloadUtil?.downloadId ?: -1L) != -1L) {
            return
        }

        if (update != null) {
            if (BuildConfig.VERSION_CODE < (update?.data?.versionId ?: 0)
                && requireActivity().isInMainMenu())
            {
                showUpdateDialog()
            }
            return
        }

        val coroutineExceptionHandler = CoroutineExceptionHandler{_, t ->
            Log.e(TAG, t.localizedMessage ?: "")
        }

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            HttpClient() {
                install(ContentNegotiation) {
                    gson()
                }
            }.use { client ->
                call = client.get(getString(R.string.api_url) + "latest")
                call?.let { response ->
                    update = response.body()
                    update?.let {
                        if (response.status.isSuccess() && it.success) {
                            withContext(Dispatchers.Main) {
                                if (BuildConfig.VERSION_CODE < (update?.data?.versionId ?: 0)
                                    && requireActivity().isInMainMenu())
                                {
                                    showUpdateDialog()
                                }
                            }
                        } else {
                            throw(Exception(it.message))
                        }
                    }
                }
            }
        }
    }

    private fun downloadUpdate(url: String) {
        viewModel.downloadUtil = DownloadUtil(requireContext(), url, getString(R.string.app_name) + ".apk")
        viewModel.downloadUtil?.setOnCompleteListener { uri, _ ->
            viewModel.downloadUri = uri
            if (requireActivity().isInMainMenu()) {
                updateApp(uri)
            }
        }
    }

    private fun updateApp(apkUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val promptInstall = Intent(Intent.ACTION_VIEW)
                .setData(apkUri)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(promptInstall)
        }
    }

    private fun showUpdateDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(update?.data?.title ?: "Update")
            .setMessage(update?.data?.description)
            .setNegativeButton(getString(R.string.disagree)) { _: DialogInterface?, _: Int -> }
            .setPositiveButton(getString(R.string.agree)) { _: DialogInterface?, _: Int ->
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    update?.let { downloadUpdate(it.data?.url ?: "") }
                } else {
                    storagePermissionResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClickListeners() {
        val connectionUtil = ConnectionUtil(requireContext())

        binding.btnSingleplayer.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            findNavController(binding.root).navigate(R.id.action_mainMenuFragment_to_gameFragment)
        }.setSound(R.raw.gamestartbtn))
        binding.btnMultiplayer.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            if (firebaseUser == null) {
                findNavController(binding.root).navigate(R.id.action_mainMenuFragment_to_signUpFragment)
                return@OnTouchListener
            }
            if (!connectionUtil.isOnline()) {
                Snackbar.make(
                    binding.root,
                    "No internet connection available.",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@OnTouchListener
            }
            findNavController(binding.root).navigate(R.id.action_mainMenuFragment_to_multiplayerFragment)
        })
        binding.btnSettings.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            SettingsFragment().show(requireActivity().supportFragmentManager, SettingsFragment.TAG)
        })
        binding.btnExit.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            requireActivity().finishAndRemoveTask()
        })
        binding.btnLeaderboard.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            findNavController(binding.root).navigate(
                if (firebaseUser == null) R.id.action_mainMenuFragment_to_scoresFragment
                else R.id.action_mainMenuFragment_to_leaderboardFragment
            )
        })
        binding.btnSocials.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.github_url))
            ).also { startActivity(it) }
        })
        binding.btnSignIn.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            findNavController(binding.root).navigate(
                if (firebaseUser == null) R.id.action_mainMenuFragment_to_signUpFragment
                else R.id.action_mainMenuFragment_to_profileFragment
            )
        })
    }

    private fun showUpdatesDisabledSnackbar(callback: () -> Unit) {
        val preferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(getString(R.string.setting_auto_update), false)
        editor.apply()
        Snackbar.make(binding.root, R.string.update_feature_unavailable, Snackbar.LENGTH_LONG)
            .setAction(R.string.update_feature_retry) {
                editor.putBoolean(getString(R.string.setting_auto_update), true)
                editor.apply()
                callback()
            }.show()
    }

    private fun goThroughDialogQueue() {
        dialogQueue
            .reversed()
            .forEach { permission ->
                showPermissionDialog(
                    permissionTextProvider = when(permission) {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                            StoragePermissionTextProvider()
                        }
                        else -> return@forEach
                    },
                    !shouldShowRequestPermissionRationale( permission ),
                    onDismiss = {
                        viewModel.dismissDialog()
                        if (!shouldShowRequestPermissionRationale( permission )) {
                            when (permission) {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                                    showUpdatesDisabledSnackbar {
                                        requireActivity().openAppSettings()
                                    }
                                }
                            }
                        }
                    },
                    onAccept = {
                        multiplePermissionResultLauncher.launch(
                            arrayOf(permission)
                        )
                    },
                    onGoToAppSettings = {
                        requireActivity().openAppSettings()
                    }
                )
            }
    }

    private fun showPermissionDialog(
        permissionTextProvider: PermissionTextProvider,
        isPermanentlyDeclined: Boolean,
        onDismiss: () -> Unit,
        onAccept: () -> Unit,
        onGoToAppSettings: () -> Unit
    ) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Permission Required")
            .setMessage(permissionTextProvider.getDescription(isPermanentlyDeclined))
            .setOnDismissListener {
                onDismiss()
            }
            .setPositiveButton(
                if (isPermanentlyDeclined) "Grant Permission" else "OK"
            ) { _, _ ->
                if (isPermanentlyDeclined) onGoToAppSettings()
                else onAccept()
            }.show()
    }

    interface PermissionTextProvider {
        fun getDescription(isPermanentlyDeclined: Boolean): String
    }

    class StoragePermissionTextProvider: PermissionTextProvider {
        override fun getDescription(isPermanentlyDeclined: Boolean): String {
            return if (isPermanentlyDeclined) {
                "It seems you permanently declined write storage permission. " +
                        "You can go to the app settings to grant it"
            } else {
                "This app needs access to your storage so that it can periodically download updates."
            }
        }

    }

    private fun Activity.isInMainMenu(): Boolean {
        return findNavController(this, R.id.fragment_container_view).currentDestination ===
                findNavController(this, R.id.fragment_container_view).findDestination(R.id.mainMenuFragment)
    }

    private fun Activity.openAppSettings() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).also(::startActivity)
    }

    companion object {
        private const val TAG = "MainMenuFragment"
    }
}