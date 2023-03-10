package com.example.tetrisapp.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentJoinLobbyBinding
import com.example.tetrisapp.model.remote.response.DefaultPayload
import com.example.tetrisapp.util.FirebaseTokenUtil
import com.example.tetrisapp.util.OnTouchListener
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.*

@AndroidEntryPoint
class JoinLobbyFragment : DialogFragment() {
    private lateinit var binding: FragmentJoinLobbyBinding
    private var token: String? = null

    private var call: HttpResponse? = null
    private var inviteCode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJoinLobbyBinding.inflate(inflater, container, false)
        if (dialog != null) {
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        }
        FirebaseTokenUtil.getFirebaseToken { token: String? ->
            if (token == null) findNavController(binding.root).navigate(R.id.action_joinLobbyFragment_to_signUpFragment)
            this.token = token
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClickListeners()

        val args = JoinLobbyFragmentArgs.fromBundle(requireArguments())
        if (args.inviteCode != null) {
            binding.etInviteCode.setText(args.inviteCode)
            binding.btnEnter.performClick()
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseTokenUtil.getFirebaseToken { token: String? -> this.token = token }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (call != null) call?.cancel()
    }

    private fun sendRequest(
        inviteCode: String
    ) {
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, t ->
            Log.e(TAG, t.stackTraceToString())
            finishLoading()
        }

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            HttpClient() {
                install(ContentNegotiation) {
                    gson()
                }
            }.use { client ->
                call = client.post(getString(R.string.api_url) + "lobby/join/${inviteCode}") {
                    setBody(
                        mapOf("idToken" to token)
                    )
                    contentType(ContentType.Application.Json)
                }
                call?.let { response ->
                    val body: DefaultPayload<String> = response.body()
                    finishLoading()
                    if (response.status.value == 401) {
                        requireActivity().runOnUiThread {
                            findNavController(binding.root).navigate(R.id.action_joinLobbyFragment_to_signUpFragment)
                        }
                    }
                    if (response.status.isSuccess() && body.success) {
                        requireActivity().runOnUiThread {
                            val action = JoinLobbyFragmentDirections.actionJoinLobbyFragmentToLobbyFragment(body.data!!)
                            findNavController(requireActivity(), R.id.fragment_container_view).navigate(action)
                        }
                    } else {
                        throw(Exception(body.message))
                    }
                }
            }
        }
    }

    private fun finishLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.btnEnter.isEnabled = true
            binding.etInviteCode.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun startLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.btnEnter.isEnabled = false
            binding.etInviteCode.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        }
    }


    private fun initOnClickListeners() {
        binding.btnEnter.setOnClickListener(OnTouchListener(requireActivity()) {
            inviteCode = binding.etInviteCode.text.toString()
            binding.etInviteCode.setText("")
            startLoading()
            sendRequest(inviteCode!!)
        })
    }

    companion object {
        const val TAG = "JoinLobbyDialogFragment"
    }
}