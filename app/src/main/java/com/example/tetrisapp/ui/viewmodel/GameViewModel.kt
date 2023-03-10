package com.example.tetrisapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tetrisapp.interfaces.PieceConfiguration
import com.example.tetrisapp.model.game.MockPlayfield
import com.example.tetrisapp.model.game.MockTetris
import com.example.tetrisapp.model.game.Piece
import com.example.tetrisapp.model.game.Tetris
import com.example.tetrisapp.model.game.configuration.PieceConfigurations
import com.example.tetrisapp.model.local.model.PlayerGameData
import com.example.tetrisapp.model.local.model.Tetromino
import com.example.tetrisapp.util.FirebaseTokenUtil
import com.example.tetrisapp.util.PusherUtil.getUserInfo
import com.pusher.client.channel.PresenceChannel
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture

class GameViewModel : ViewModel() {
    var token: String? = null
    var timer: Int = 0
    var multiplayerTimer: Int = 0
    lateinit var configuration: PieceConfiguration
    lateinit var game: Tetris
    var countdown = 0
    var countdownRemaining = 0

    val executor = Executors.newSingleThreadScheduledExecutor()
    var futureMoveRight: ScheduledFuture<*>? = null
    var futureMoveLeft: ScheduledFuture<*>? = null
    var countdownFuture: Future<*>? = null
    var timerFuture: ScheduledFuture<*>? = null

    val userGameDataMap: MutableMap<String, PlayerGameData> = HashMap()
    val mockTetris = MockTetris()
    val mockPlayfield = MockPlayfield()
    val mockTetrisSpectate = MockTetris()
    val mockPlayfieldSpectate = MockPlayfield()

    var placement = 1

    init {
        FirebaseTokenUtil.getFirebaseToken { token: String? -> this.token = token }
        mockTetris.playfield = mockPlayfield
        mockTetrisSpectate.playfield = mockPlayfieldSpectate
    }

    fun updateMockTetris(channel: PresenceChannel, currentPlayerUid: String): String? {
        val userGameValues: MutableList<PlayerGameData> = userGameDataMap.values.toMutableList()

        userGameValues.sortBy { data -> -data.score }
        userGameValues.filter { data -> data.isPlaying }

        var bestScoringPlayer = userGameValues.lastOrNull()
        if (bestScoringPlayer?.userId == currentPlayerUid) {
            bestScoringPlayer = if (userGameValues.size >= 2) userGameValues[userGameValues.size - 2] else null
        }

        if (bestScoringPlayer == null) return null
        val configurationName = getUserInfo(channel, bestScoringPlayer.userId)?.configuration ?: PieceConfigurations.DEFAULT.name
        val configuration = PieceConfigurations.valueOf(configurationName).configuration

        mockTetris.configuration = configuration

        // Init current piece
        val piece = mockTetris.configuration[bestScoringPlayer.tetromino.name].copy()
        piece.matrix = bestScoringPlayer.tetromino.matrix
        piece.col = bestScoringPlayer.tetromino.x
        piece.row = bestScoringPlayer.tetromino.y

        // Init current piece shadow
        val pieceShadow = mockTetris.configuration[bestScoringPlayer.tetrominoShadow.name].copy()
        pieceShadow.matrix = bestScoringPlayer.tetrominoShadow.matrix
        pieceShadow.col = bestScoringPlayer.tetrominoShadow.x
        pieceShadow.row = bestScoringPlayer.tetrominoShadow.y

        // Set opponent related game views
        mockTetris.score = bestScoringPlayer.score
        mockTetris.level = bestScoringPlayer.level
        mockTetris.lines = bestScoringPlayer.lines
        mockTetris.currentPiece = piece
        mockTetris.shadow = pieceShadow
        mockPlayfield.state = bestScoringPlayer.playfield
        return bestScoringPlayer.userId
    }

    fun updateSpectatorMockTetris(channel: PresenceChannel): String? {
        val userGameValues: MutableList<PlayerGameData> = userGameDataMap.values.toMutableList()

        userGameValues.sortBy { data -> -data.score }
        userGameValues.filter { data -> data.isPlaying }

        val bestScoringPlayer = userGameValues.lastOrNull() ?: return null

        val configurationName = getUserInfo(channel, bestScoringPlayer.userId)?.configuration ?: PieceConfigurations.DEFAULT.name
        val configuration = PieceConfigurations.valueOf(configurationName).configuration

        mockTetrisSpectate.configuration = configuration

        // Init current piece
        val piece = mockTetrisSpectate.configuration[bestScoringPlayer.tetromino.name].copy()
        piece.matrix = bestScoringPlayer.tetromino.matrix
        piece.col = bestScoringPlayer.tetromino.x
        piece.row = bestScoringPlayer.tetromino.y

        // Init current piece shadow
        val pieceShadow =
            mockTetrisSpectate.configuration[bestScoringPlayer.tetrominoShadow.name].copy()
        pieceShadow.matrix = bestScoringPlayer.tetrominoShadow.matrix
        pieceShadow.col = bestScoringPlayer.tetrominoShadow.x
        pieceShadow.row = bestScoringPlayer.tetrominoShadow.y

        // Set opponent related game views
        mockTetrisSpectate.score = bestScoringPlayer.score
        mockTetrisSpectate.level = bestScoringPlayer.level
        mockTetrisSpectate.lines = bestScoringPlayer.lines
        mockTetrisSpectate.shadow = pieceShadow
        mockTetrisSpectate.currentPiece = piece
        mockPlayfieldSpectate.state = bestScoringPlayer.playfield
        return bestScoringPlayer.userId
    }

    fun getPlacement(gameMode: String): Int {
        return if (gameMode == "battleRoyale") {
            placement
        } else {
            val userGameValues: MutableList<PlayerGameData> = userGameDataMap.values.toMutableList()
            userGameValues.sortBy { data -> -data.score }

            var placement = userGameValues.size + 1
            for (i in userGameValues.indices) {
                if (game.score >= userGameValues[i].score) {
                    placement = i + 1
                    break
                }
            }
            placement
        }
    }

    fun countPlaying(): Int {
        return userGameDataMap.values.count { it.isPlaying } + if (game.isGameOver) 0 else 1
    }

    fun getGameData(userId: String?): PlayerGameData {
        var placement: Int? = null
        if (game.isGameOver) {
            placement = userGameDataMap.values.stream()
                .reduce(0, { x: Int, y: PlayerGameData ->
                    x + if (y.isPlaying) 1 else 0 })
                { a: Int, b: Int ->
                    a + b
                } + 1
        }

        return PlayerGameData(
            userId,
            game.score,
            game.lines,
            game.level,
            game.combo,
            pieceToTetromino(game.currentPiece!!),
            pieceToTetromino(game.shadow!!),
            game.heldPiece,
            game.tetrominoSequence.toTypedArray(),
            game.playfield.state,
            !game.isGameOver,
            placement
        )
    }

    private fun pieceToTetromino(piece: Piece): Tetromino {
        return Tetromino(
            piece.name,
            piece.matrix,
            piece.col,
            piece.row
        )
    }

    companion object {
        const val TAG = "GameViewModel"
    }
}