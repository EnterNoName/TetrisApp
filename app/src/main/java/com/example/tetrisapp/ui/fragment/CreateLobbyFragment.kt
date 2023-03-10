package com.example.tetrisapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentCreateLobbyBinding
import com.example.tetrisapp.model.remote.response.DefaultPayload
import com.example.tetrisapp.ui.activity.MainActivity
import com.example.tetrisapp.util.FirebaseTokenUtil
import com.example.tetrisapp.util.OnTouchListener
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class CreateLobbyFragment : Fragment() {
    private lateinit var binding: FragmentCreateLobbyBinding
    private var token: String? = null
    private var call: HttpResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateLobbyBinding.inflate(inflater, container, false)
        FirebaseTokenUtil.getFirebaseToken { token: String? ->
            if (token == null) findNavController(binding.root).navigate(R.id.action_createLobbyFragment_to_signUpFragment)
            this.token = token
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        call?.cancel()
    }

    private fun initOnClickListeners() {
        binding.chipHighScore.setOnCheckedChangeListener { _, checked ->
            binding.linearLayout3.visibility = if (checked) View.VISIBLE else View.GONE
        }

        binding.btnBack.setOnClickListener(OnTouchListener(requireActivity() as MainActivity) {
            findNavController(binding.root).popBackStack()
        })
        binding.btnResetSettings.setOnClickListener(OnTouchListener(requireActivity() as MainActivity) {
            binding.countdownSlider.value = 3.0f
            binding.playerLimitSlider.value = 2.0f
            binding.switchEnablePause.isChecked = false
        })
        binding.btnCreateLobby.setOnClickListener(OnTouchListener(requireActivity() as MainActivity) {
            val countdown = binding.countdownSlider.value.roundToInt()
            val playerLimit = binding.playerLimitSlider.value.roundToInt()
            val timer = binding.timerSlider.value.roundToInt()
            val enablePause = binding.switchEnablePause.isChecked
            val gameMode = if (binding.chipHighScore.isChecked) "highestScore" else "battleRoyale"
            startLoading()

            sendRequest(countdown, playerLimit, timer, enablePause, gameMode)
        })
    }

    private fun sendRequest(
        countdown: Int,
        playerLimit: Int,
        timer: Int,
        enablePause: Boolean,
        gameMode: String
    ) {
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, t ->
            Log.e(TAG, t.localizedMessage ?: "")
            finishLoading()
        }

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            HttpClient() {
                install(ContentNegotiation) {
                    gson()
                }
            }.use { client ->
                call = client.post(getString(R.string.api_url) + "lobby/create") {
                    setBody(
                        mapOf(
                            "idToken" to token,
                            "countdown" to countdown,
                            "limit" to playerLimit,
                            "enablePause" to enablePause,
                            "timer" to timer,
                            "gameMode" to gameMode
                        )
                    )
                    contentType(ContentType.Application.Json)
                }
                call?.let { response ->
                    val body: DefaultPayload<String> = response.body()
                    finishLoading()
                    if (response.status.value == 401) {
                        findNavController(binding.root).navigate(R.id.action_createLobbyFragment_to_signUpFragment)
                    }
                    if (response.status.isSuccess() && body.success) {
                        withContext(Dispatchers.Main) {
                            val action = CreateLobbyFragmentDirections.actionCreateLobbyFragmentToLobbyFragment()
                            action.inviteCode = body.data ?: ""
                            findNavController(binding.root).navigate(action)
                        }
                    } else {
                        throw(Exception(body.message))
                    }
                }
            }
        }
    }


    private fun startLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.countdownSlider.isEnabled = false
            binding.playerLimitSlider.isEnabled = false
            binding.switchEnablePause.isEnabled = false
            binding.btnResetSettings.isEnabled = false
            binding.btnCreateLobby.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun finishLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.countdownSlider.isEnabled = true
            binding.playerLimitSlider.isEnabled = true
            binding.switchEnablePause.isEnabled = true
            binding.btnResetSettings.isEnabled = true
            binding.btnCreateLobby.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "CreateLobbyFragment"
    }
}