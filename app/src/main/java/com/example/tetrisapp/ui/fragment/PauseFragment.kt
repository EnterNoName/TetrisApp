package com.example.tetrisapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentPauseBinding
import com.example.tetrisapp.ui.activity.MainActivity
import com.example.tetrisapp.util.OnTouchListener
import com.example.tetrisapp.util.PusherUtil
import com.example.tetrisapp.util.PusherUtil.bindResume
import com.example.tetrisapp.util.PusherUtil.unbindResume
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pusher.client.channel.PresenceChannel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PauseFragment : Fragment() {
    private lateinit var binding: FragmentPauseBinding
    private lateinit var args: PauseFragmentArgs
    private lateinit var channel: PresenceChannel
    private var dialogOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPauseBinding.inflate(inflater, container, false)
        args = PauseFragmentArgs.fromBundle(requireArguments())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClickListeners()
        val pusher = (requireActivity() as MainActivity).pusher
        if (args.lobbyCode != null && pusher != null) {
            channel = pusher.getPresenceChannel("presence-" + args.lobbyCode)
            bindResume(channel) { multiplayerResume() }
        }
    }

    private fun multiplayerResume() {
        unbindResume(channel)
        requireActivity().runOnUiThread { findNavController(binding.root).popBackStack() }
    }

    private fun initOnClickListeners() {
        binding.btnResume.setOnClickListener(OnTouchListener(requireActivity()) {
            val pusher = (requireActivity() as MainActivity).pusher
            if (args.lobbyCode != null && pusher != null) {
                multiplayerResume()
                channel.trigger(PusherUtil.GAME_RESUME, "")
            } else {
                findNavController(binding.root).popBackStack()
            }
        })
        binding.btnLeave.setOnClickListener(OnTouchListener(requireActivity()) {
            confirmExit()
        })
    }

    private fun confirmExit() {
        if (!dialogOpen) {
            dialogOpen = true
            MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.exit_dialog_title))
                .setMessage(getString(R.string.exit_dialog_description))
                .setNegativeButton(getString(R.string.disagree)) { _, _ ->
                    dialogOpen = false
                }
                .setPositiveButton(getString(R.string.agree)) { _, _ ->
                    findNavController(binding.root).navigate(R.id.action_pauseFragment_to_mainMenuFragment)
                }
                .show()
        }
    }
}