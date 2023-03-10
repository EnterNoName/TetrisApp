package com.example.tetrisapp.ui.fragment

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.media.PlaybackParams
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewStub
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.SidebarMultiplayerBinding
import com.example.tetrisapp.model.game.Tetris
import com.example.tetrisapp.model.game.multiplayer.GameOverMultiplayerParcel
import com.example.tetrisapp.model.local.model.GameOverData
import com.example.tetrisapp.model.local.model.PlayerGameData
import com.example.tetrisapp.ui.activity.MainActivity
import com.example.tetrisapp.util.OnTouchListener
import com.example.tetrisapp.util.PusherUtil
import com.example.tetrisapp.util.PusherUtil.bindGameOver
import com.example.tetrisapp.util.PusherUtil.bindPause
import com.example.tetrisapp.util.PusherUtil.bindPlayerGameData
import com.example.tetrisapp.util.PusherUtil.bindPlayerLost
import com.example.tetrisapp.util.PusherUtil.getUserInfo
import com.example.tetrisapp.util.PusherUtil.unbindGameOver
import com.example.tetrisapp.util.PusherUtil.unbindPause
import com.example.tetrisapp.util.PusherUtil.unbindPlayerGameData
import com.example.tetrisapp.util.PusherUtil.unbindPlayerLost
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.pusher.client.channel.PresenceChannel
import com.pusher.client.channel.PresenceChannelEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.random.Random

@AndroidEntryPoint
class GameMultiplayerFragment : GameFragment() {
    private lateinit var sidebarBinding: SidebarMultiplayerBinding
    private lateinit var args: GameMultiplayerFragmentArgs

    private lateinit var channel: PresenceChannel
    private var spectating = false
    private var gameEnded = false
    private var disconnectListener: PresenceChannelEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = GameMultiplayerFragmentArgs.fromBundle(requireArguments())
        viewModel.countdown = args.gameData.countdown
        viewModel.countdownRemaining = viewModel.countdown
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnPause.visibility = if (args.gameData.enablePause) View.VISIBLE else View.GONE


    }

    override fun startTimer() {
        super.startTimer()
        if (args.gameData.gameMode == "highestScore") {
            viewModel.multiplayerTimer = args.gameData.timer * 60
            viewModel.executor.scheduleAtFixedRate(
                {
                    lifecycleScope.launch(Dispatchers.Main) {
                        viewModel.multiplayerTimer -= 1
                        val minutes = viewModel.multiplayerTimer / 60
                        val seconds = viewModel.multiplayerTimer % 60
                        binding.tvTimer.text = "Time left:\n${minutes}:${seconds}"
                    }
                },
                0L,
                1L,
                TimeUnit.SECONDS
            )
        }
    }

    override fun inflateSidebar() {
        binding.stub.setOnInflateListener { _: ViewStub?, inflated: View? ->
            sidebarBinding = SidebarMultiplayerBinding.bind(
                inflated!!
            )
            sidebarBinding.gameViewCompetitor.game = viewModel.mockTetris
        }
        binding.stub.layoutResource = R.layout.sidebar_multiplayer
        binding.stub.inflate()
    }

    override fun onResume() {
        super.onResume()
        initMultiplayerGameView()
    }

    override fun onPause() {
        super.onPause()
        unbindGameOver(channel)
        unbindPlayerGameData(channel)
        unbindPlayerLost(channel)
        unbindPause(channel)
        channel.unbindGlobal(disconnectListener)
    }

    @SuppressLint("SetTextI18n")
    override fun updateScoreboard() {
        lifecycleScope.launch(Dispatchers.Main) {
            sidebarBinding.score.text = viewModel.game.score.toString()
            sidebarBinding.level.text = viewModel.game.level.toString()
            sidebarBinding.lines.text = viewModel.game.lines.toString()
            sidebarBinding.combo.text = viewModel.game.combo.toString()
        }
        val playbackSpeed = 2 - viewModel.game.speed / Tetris.DEFAULT_SPEED.toFloat()
        gameMusic?.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
    }

    override fun updatePieceViews() {
        lifecycleScope.launch(Dispatchers.Main) {
            sidebarBinding.pvNext1.setPiece(viewModel.configuration[viewModel.game.tetrominoSequence[0]].copy())
            sidebarBinding.pvNext2.setPiece(viewModel.configuration[viewModel.game.tetrominoSequence[1]].copy())
            viewModel.game.heldPiece?.let { piece ->
                sidebarBinding.pvHold.setPiece(viewModel.configuration[piece].copy())
            }
        }
    }

    override fun initPauseOnClickListener() {
        if (!args.gameData.enablePause) return
        binding.btnPause.setOnClickListener(OnTouchListener(requireActivity()) {
            multiplayerPause()
            channel.trigger(PusherUtil.GAME_PAUSE, null)
        })
    }

    override fun initGameListeners() {
        super.initGameListeners()
        viewModel.game.onMoveCallback = {
            binding.gameView.postInvalidate()
            sendGameData()
        }
        viewModel.game.onGameOverCallback = {
            switchToSpectatorMode()
            declareLoss()
        }
    }

    override fun initSidebarOnClickListeners() {
        sidebarBinding.pvHold.setOnClickListener {
            viewModel.game.hold()
            mediaHelper.playSound(R.raw.click, sfxVolume)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initMultiplayerGameView() {
        val pusher = (activity as MainActivity).pusher ?: return
        channel = pusher.getPresenceChannel("presence-${args.inviteCode}")

        bindPlayerGameData(channel) { data: PlayerGameData ->
            var currentPlayerUid = channel.me.id
            if (spectating) {
                currentPlayerUid = updateSpectatorView(data)
            } else if (args.gameData.gameMode == "battleRoyale") {
                processPayload(currentPlayerUid, data)
            }

            updateMultiplayerSideView(data, currentPlayerUid)
        }

        if (args.gameData.enablePause) {
            bindPause(channel) { multiplayerPause() }
        }

        bindGameOver(channel) { data: GameOverData ->
            try {
                multiplayerGameOver(data.userId)
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage ?: "")
            }
        }

        bindPlayerLost(channel) { data: PlayerGameData ->
            viewModel.userGameDataMap[data.userId] = data
            var currentPlayerUid = channel.me.id
            if (spectating) currentPlayerUid = updateSpectatorView(data)
            updateMultiplayerSideView(data, currentPlayerUid)
            args.gameData.playerList.remove(data.userId)
        }

        disconnectListener = PusherUtil.createEventListener(
            onUserUnsubscribed = {_, user ->
                val data = viewModel.userGameDataMap[user.id] ?: return@createEventListener
                viewModel.userGameDataMap[data.userId] = PlayerGameData(
                    data.userId,
                    data.score,
                    data.lines,
                    data.level,
                    data.combo,
                    data.tetromino,
                    data.tetrominoShadow,
                    data.heldTetromino,
                    data.tetrominoSequence,
                    data.playfield,
                    false,
                    viewModel.countPlaying()
                )
                args.gameData.playerList.remove(user.id)
            }
        )

        channel.bindGlobal(disconnectListener)
    }

    private fun processPayload(
        currentPlayerUid: String,
        data: PlayerGameData
    ) {
        val selfIndex = args.gameData.playerList.indexOf(currentPlayerUid)
        val senderUid =
            if (selfIndex > 0)
                args.gameData.playerList[selfIndex - 1]
            else
                args.gameData.playerList[args.gameData.playerList.size - 1]

        if (senderUid == data.userId) {
            val payload = data.lines - (viewModel.userGameDataMap[data.userId]?.lines ?: 0)
            if (payload < 1) return

            val emptyCol = Random.nextInt(viewModel.game.playfield.state[0].size)
            val array = arrayOfNulls<String>(viewModel.game.playfield.state[0].size)
                .mapIndexed { i, _ ->
                    if (i == emptyCol) null else "XXX"
                }.toTypedArray()

            viewModel.game.playfield.state.mapIndexed { i, _ ->
                val newRow = viewModel.game.playfield.state.getOrNull(i + payload) ?: array.copyOf()
                viewModel.game.playfield.state[i] = newRow
            }

            viewModel.game.calculateShadow()
        }
    }

    private fun declareLoss() {
        try {
            val gson = Gson()
            channel.trigger(
                PusherUtil.PLAYER_DECLARE_LOSS, gson.toJson(viewModel.getGameData(channel.me.id))
            )
            viewModel.placement = viewModel.countPlaying() + 1
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage ?: "")
        }
    }

    private fun sendGameData() {
        try {
            val gson = Gson()
            channel.trigger(
                PusherUtil.PLAYER_UPDATE_DATA, gson.toJson(viewModel.getGameData(channel.me.id))
            )
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage ?: "")
        }
    }

    private fun multiplayerPause() {
        requireActivity().runOnUiThread {
            val action =
                GameMultiplayerFragmentDirections.actionGameMultiplayerFragmentToPauseFragment()
            action.lobbyCode = args.inviteCode
            findNavController(binding.root).navigate(action)
        }
    }

    private fun multiplayerGameOver(winnerUid: String) {
        gameEnded = if (gameEnded) {
            return
        } else {
            true
        }

        lifecycleScope.launch(Dispatchers.Main) {
            // Setting game over action parameters
            val action = GameMultiplayerFragmentDirections.actionGameMultiplayerFragmentToGameOverMultiplayerFragment(
                    GameOverMultiplayerParcel(
                        score = viewModel.game.score,
                        level = viewModel.game.level,
                        lines = viewModel.game.lines,
                        timer = viewModel.timer,
                        placement = viewModel.getPlacement(args.gameData.gameMode),
                        inviteCode = args.inviteCode,
                        winnerUserId = winnerUid
                    )
                )
            findNavController(binding.root).navigate(action)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSpectatorView(data: PlayerGameData): String? {
        viewModel.userGameDataMap[data.userId] = data
        val spectatedPlayerUid = viewModel.updateSpectatorMockTetris(channel)
        val spectatedPlayerData =
            viewModel.userGameDataMap.getOrDefault(spectatedPlayerUid, null) ?: return null
        val spectatedPlayerInfo = getUserInfo(
            channel, spectatedPlayerUid ?: ""
        ) ?: return null
        binding.gameView.postInvalidate()
        requireActivity().runOnUiThread {
            binding.tvSpectate.text = """
                Spectating:
                ${spectatedPlayerInfo.name}
                """.trimIndent()
            sidebarBinding.score.text = spectatedPlayerData.score.toString()
            sidebarBinding.level.text = spectatedPlayerData.level.toString()
            sidebarBinding.lines.text = spectatedPlayerData.lines.toString()
            sidebarBinding.combo.text = spectatedPlayerData.combo.toString()

            if (spectatedPlayerData.tetrominoSequence.size >= 2) {
                sidebarBinding.pvNext1.setPiece(viewModel.mockTetrisSpectate.configuration[spectatedPlayerData.tetrominoSequence[0]].copy())
                sidebarBinding.pvNext2.setPiece(viewModel.mockTetrisSpectate.configuration[spectatedPlayerData.tetrominoSequence[1]].copy())
            }
            if (spectatedPlayerData.heldTetromino != null) {
                sidebarBinding.pvHold.setPiece(viewModel.mockTetrisSpectate.configuration[spectatedPlayerData.heldTetromino].copy())
            } else {
                sidebarBinding.pvHold.setPiece(null)
            }
        }
        return spectatedPlayerUid
    }

    @SuppressLint("SetTextI18n")
    private fun updateMultiplayerSideView(data: PlayerGameData, currentPlayerUid: String?) {
        // Save received user game data
        viewModel.userGameDataMap[data.userId] = data
        val bestScoringPlayerUid = viewModel.updateMockTetris(channel, currentPlayerUid ?: "")
        val bestScoringPlayer = viewModel.userGameDataMap.getOrDefault(bestScoringPlayerUid, null)
        if (bestScoringPlayer == null) {
            hideCompetitorSideView()
            return
        }
        showCompetitorSideView()
        val bestScoringPlayerInfo = getUserInfo(
            channel, bestScoringPlayer.userId
        ) ?: return
        sidebarBinding.gameViewCompetitor.postInvalidate()
        requireActivity().runOnUiThread {
            sidebarBinding.tvScoreCompetitor.text = """
                ${bestScoringPlayerInfo.name}'s
                Score:
                """.trimIndent()
            sidebarBinding.scoreCompetitor.text = bestScoringPlayer.score.toString() + ""
        }
    }

    private fun hideCompetitorSideView() {
        lifecycleScope.launch(Dispatchers.Main) {
            sidebarBinding.gameViewCompetitor.visibility = View.GONE
            sidebarBinding.tvScoreCompetitor.visibility = View.GONE
            sidebarBinding.scoreCompetitor.visibility = View.GONE
        }
    }

    private fun showCompetitorSideView() {
        lifecycleScope.launch(Dispatchers.Main) {
            sidebarBinding.gameViewCompetitor.visibility = View.VISIBLE
            sidebarBinding.tvScoreCompetitor.visibility = View.VISIBLE
            sidebarBinding.scoreCompetitor.visibility = View.VISIBLE
        }
    }

    private fun switchToSpectatorMode() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.controls.visibility = View.GONE
            binding.btnPause.visibility = View.GONE
            sidebarBinding.cvHold.isClickable = false
            binding.gameView.game = viewModel.mockTetrisSpectate
            binding.tvSpectate.visibility = View.VISIBLE
            spectating = true
        }
    }

    override fun confirmExit() {
        viewModel.game.setPause(true)
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.AlertDialogTheme
        ).setTitle(getString(R.string.exit_dialog_title))
            .setMessage(getString(R.string.exit_dialog_description))
            .setOnDismissListener { viewModel.game.setPause(false) }
            .setNegativeButton(getString(R.string.disagree)) { _: DialogInterface?, _: Int ->
                viewModel.game.setPause(false)
            }.setPositiveButton(getString(R.string.agree)) { _: DialogInterface?, _: Int ->
                findNavController(binding.root).navigate(R.id.action_gameMultiplayerFragment_to_mainMenuFragment)
            }.show()
    }

    companion object {
        private const val TAG = "GameMultiplayerFragment"
    }
}