package com.example.tetrisapp.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentMultiplayerBinding
import com.example.tetrisapp.util.OnTouchListener

class MultiplayerFragment : DialogFragment() {
    private lateinit var binding: FragmentMultiplayerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultiplayerBinding.inflate(inflater, container, false)
        if (dialog != null) {
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClickListeners()
    }

    private fun initOnClickListeners() {
        binding.btnJoinLobby.setOnClickListener(OnTouchListener(requireActivity()) {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_multiplayerFragment_to_joinLobbyFragment)
        })
        binding.btnCreateLobby.setOnClickListener(OnTouchListener(requireActivity()) {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_multiplayerFragment_to_createLobbyFragment)
        })
    }

    companion object {
        const val TAG = "MultiplayerFragment"
    }
}