package com.example.tetrisapp.ui.fragment

import android.annotation.SuppressLint
import android.content.*
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentLobbyBinding
import com.example.tetrisapp.model.local.model.GameStartedData
import com.example.tetrisapp.model.local.model.UserInfo
import com.example.tetrisapp.ui.activity.MainActivity
import com.example.tetrisapp.ui.adapters.UsersRecyclerViewAdapter
import com.example.tetrisapp.ui.viewmodel.LobbyViewModel
import com.example.tetrisapp.util.OnTouchListener
import com.example.tetrisapp.util.PusherUtil
import com.example.tetrisapp.util.convertDpToPixel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.pusher.client.channel.PresenceChannel
import com.pusher.client.channel.PresenceChannelEventListener
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LobbyFragment : Fragment() {
    private lateinit var binding: FragmentLobbyBinding
    private lateinit var args: LobbyFragmentArgs
    private val viewModel by viewModels<LobbyViewModel>()

    private lateinit var listener: PresenceChannelEventListener
    private lateinit var channel: PresenceChannel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handle back button press
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmExit()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLobbyBinding.inflate(inflater, container, false)
        args = LobbyFragmentArgs.fromBundle(requireArguments())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.usersLiveData.observe(viewLifecycleOwner) {
            binding.list.adapter = UsersRecyclerViewAdapter(it)
        }

        startLoading()
        initOnClickListeners()
        initPusherChannelListeners()
        updateUI()
    }

    private fun confirmExit() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(getString(R.string.lobby_exit_alert_title))
            .setMessage(getString(R.string.lobby_exit_alert_message))
            .setNegativeButton(getString(R.string.disagree)) { _: DialogInterface?, _: Int -> }
            .setPositiveButton(getString(R.string.agree)) { _: DialogInterface?, _: Int -> exitLobby() }
            .show()
    }


    private fun initOnClickListeners() {
        binding.btnExitLobby.setOnClickListener(OnTouchListener(requireActivity()) {
            confirmExit()
        })
        binding.btnStartGame.setOnClickListener(OnTouchListener(requireActivity()) {
            sendGameStartRequest()
        })

        // Copies invite code to clip board
        binding.btnActionMenu.setOnClickListener(OnTouchListener(requireActivity()) { v: View ->
            showMenu(v, R.menu.menu_invite_code_action)
        })
        binding.btnBack.setOnClickListener(OnTouchListener(requireActivity()) {
            exitLobby()
        })
    }

    private fun sendGameStartRequest() {
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, t ->
            finishLoading()
            Log.e(TAG, t.localizedMessage ?: "")
        }

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            HttpClient() {
                install(ContentNegotiation) {
                    gson()
                }
            }.use { client ->
                client.post(getString(R.string.api_url) + "game/start") {
                    setBody(
                        mapOf("idToken" to viewModel.token)
                    )
                    contentType(ContentType.Application.Json)
                }
            }
        }
    }

    private fun startLoading() {
        requireActivity().runOnUiThread {
            binding.btnStartGame.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun finishLoading() {
        requireActivity().runOnUiThread {
            binding.btnStartGame.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

    @SuppressLint("NonConstantResourceId", "RestrictedApi")
    private fun showMenu(v: View, resId: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(resId, popup.menu)
        val menuBuilder = popup.menu as MenuBuilder
        menuBuilder.setOptionalIconsVisible(true)
        for (item in menuBuilder.visibleItems) {
            val iconMarginPx = convertDpToPixel(requireActivity().resources, 4f)
            if (item.icon != null) {
                item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
            }
        }
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.copy_code -> {
                    val clipboard =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip =
                        ClipData.newPlainText(getString(R.string.invite_code), args.inviteCode)
                    clipboard.setPrimaryClip(clip)
                    Snackbar.make(binding.root, "Copied!", Snackbar.LENGTH_SHORT).show()
                }
                R.id.share_code -> {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.share_invite_code_subject)
                    )
                    shareIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        "https://tetrisapp.com/invite/${args.inviteCode}"
                    )
                    startActivity(
                        Intent.createChooser(
                            shareIntent,
                            getString(R.string.share_using)
                        )
                    )
                }
            }
            true
        }
        popup.show()
    }

    private fun initPusherChannelListeners() {
        val pusher = (requireActivity() as MainActivity).pusher ?: return

        listener = PusherUtil.createEventListener(
            onUserSubscribed = { _, user ->
                viewModel.addUser(
                    Gson().fromJson(user.info, UserInfo::class.java)
                )
            },
            onUserUnsubscribed = { _, user ->
                viewModel.removeUser(
                    Gson().fromJson(user.info, UserInfo::class.java)
                )
            },
            onAuthenticationFailed = { _, e ->
                Log.e(TAG, e.localizedMessage ?: "")
                exitLobby()
            },
            onSubscribed = {
                finishLoading()
            }
        )

        channel = PusherUtil.getPresenceChannel(
            pusher = pusher,
            channelName = "presence-${args.inviteCode}",
            listener = listener
        )

        PusherUtil.bindGameStart(channel) { data: GameStartedData ->
            Log.d(TAG, Gson().toJson(data))
            lifecycleScope.launch(Dispatchers.Main) {
                PusherUtil.unbindGameStart(channel)
                channel.unbindGlobal(listener)

                val action = LobbyFragmentDirections.actionLobbyFragmentToGameMultiplayerFragment(
                    args.inviteCode,
                    data
                )
                findNavController(binding.root).navigate(action)
            }
        }
    }

    private fun updateUI() {
        binding.inviteCode.text = args.inviteCode
        binding.tvLobbyTitle.text =
            if (viewModel.lobbyOwnerName != null)
                getString(R.string.users_lobby).format(viewModel.lobbyOwnerName)
            else
                getString(R.string.your_lobby)
    }

    private fun exitLobby() {
        if (findNavController(binding.root).currentDestination ===
            findNavController(binding.root).findDestination(R.id.lobbyFragment)
        ) {
            val pusher = (requireActivity() as MainActivity).pusher

            pusher?.unsubscribe("presence-${args.inviteCode}")
            findNavController(binding.root).popBackStack()
        }
    }

    companion object {
        const val TAG = "LobbyFragment"
    }
}