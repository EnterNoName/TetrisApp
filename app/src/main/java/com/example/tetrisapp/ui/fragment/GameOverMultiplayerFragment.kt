package com.example.tetrisapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentGameOverBinding
import com.example.tetrisapp.model.local.model.GameStartedData
import com.example.tetrisapp.ui.activity.MainActivity
import com.example.tetrisapp.util.FirebaseTokenUtil
import com.example.tetrisapp.util.OnTouchListener
import com.example.tetrisapp.util.PusherUtil.bindGameStart
import com.example.tetrisapp.util.PusherUtil.getUserInfo
import com.example.tetrisapp.util.PusherUtil.unbindGameStart
import com.pusher.client.channel.PresenceChannel
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
class GameOverMultiplayerFragment : GameOverFragment() {
    private lateinit var args: GameOverMultiplayerFragmentArgs
    var channel: PresenceChannel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameOverBinding.inflate(inflater, container, false)
        args = GameOverMultiplayerFragmentArgs.fromBundle(requireArguments())
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        unbindGameStart(channel!!)
    }

    override fun updateUI() {
        val pusher = (requireActivity() as MainActivity).pusher
        binding.score.text = args.data.score.toString()
        binding.level.text = args.data.level.toString()
        binding.lines.text = args.data.lines.toString()
        channel = pusher?.getPresenceChannel("presence-" + args.data.inviteCode)
        val winnerUserInfo = channel?.let { getUserInfo(it, args.data.winnerUserId) }

        if (args.data.score > 0) {
            insertScoreInDB(args.data.score, args.data.level, args.data.lines, args.data.timer)
        }

        binding.tvHighScore.text = if (args.data.placement == 1) getString(R.string.game_over_player_won)
        else getString(R.string.game_over_player_lost).format(args.data.placement, winnerUserInfo?.name)
    }

    override fun initClickListeners() {
        bindGameStart(channel!!) { data: GameStartedData ->
            requireActivity().runOnUiThread {
                val action = GameOverMultiplayerFragmentDirections
                        .actionGameOverMultiplayerFragmentToGameMultiplayerFragment(
                            args.data.inviteCode,
                            data
                        )
                findNavController(binding.root).navigate(action)
            }
        }
        binding.btnLeave.setOnClickListener(OnTouchListener(requireActivity()) {
            val pusher = (requireActivity() as MainActivity).pusher
            pusher?.unsubscribe("presence-" + args.data.inviteCode)
            findNavController(binding.root).navigate(R.id.action_gameOverMultiplayerFragment_to_mainMenuFragment)
        })
        binding.btnRetry.setOnClickListener(OnTouchListener(requireActivity()) {
            FirebaseTokenUtil.getFirebaseToken { token: String? -> sendGameStartRequest(token ?: "")}
        })
        binding.btnShare.setOnClickListener(OnTouchListener(requireActivity()) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            shareIntent.putExtra(
                Intent.EXTRA_TEXT, String.format(
                    getString(R.string.share_text),
                    args.data.score
                )
            )
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)))
        })
    }

    private fun sendGameStartRequest(token: String) {
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, t ->
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
                        mapOf("idToken" to token)
                    )
                    contentType(ContentType.Application.Json)
                }
            }
        }
    }
}