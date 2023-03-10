package com.example.tetrisapp.util

import android.util.Log
import com.example.tetrisapp.model.local.model.GameOverData
import com.example.tetrisapp.model.local.model.GameStartedData
import com.example.tetrisapp.model.local.model.PlayerGameData
import com.example.tetrisapp.model.local.model.UserInfo
import com.google.gson.Gson
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.*
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpChannelAuthorizer

object PusherUtil {
    const val TAG = "Pusher"
    const val PLAYER_UPDATE_DATA = "client-player-update-data"
    const val PLAYER_DECLARE_LOSS = "client-player-declare-loss"
    const val GAME_PAUSE = "client-game-pause"
    const val GAME_RESUME = "client-game-resume"
    const val GAME_START = "game-started"
    const val GAME_OVER = "game-ended"
    
    private var playerGameDataCallback: SubscriptionEventListener? = null
    private var playerLostCallback: SubscriptionEventListener? = null
    private var gameStartCallback: SubscriptionEventListener? = null
    private var gameOverCallback: SubscriptionEventListener? = null
    private var gamePauseCallback: SubscriptionEventListener? = null
    private var gameResumeCallback: SubscriptionEventListener? = null

    fun createPusherInstance(authenticationEndpoint: String, configuration: String?, token: String): Pusher {
        val options = PusherOptions()
        options.setCluster("ap1")
        options.channelAuthorizer = HttpChannelAuthorizer(authenticationEndpoint, PusherConnectionFactory(token, configuration))

        val pusher = Pusher("754b90f0401e477f519a", options)

        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.i(TAG, """State changed from ${change.previousState} to ${change.currentState}""")
            }

            override fun onError(message: String?, code: String?, e: Exception?) {
                Log.i(TAG, """
                         There was a problem connecting! 
                         code: $code
                         message: $message
                         Exception: $e
                         """.trimIndent())
            }
        }, ConnectionState.ALL)

        return pusher
    }

    fun findUser(
        channel: PresenceChannel,
        filter: (User) -> Boolean
    ): User? {
        for (user in channel.users) {
            if (filter(user)) {
                return user
            }
        }
        return null
    }
    
    fun getUserInfo(channel: PresenceChannel, userId: String): UserInfo? {
        val gson = Gson()
        if (channel.me.id == userId) {
            return gson.fromJson(channel.me.info, UserInfo::class.java)
        }
        for (user in channel.users) {
            if (user.id == userId) {
                return gson.fromJson(user.info, UserInfo::class.java)
            }
        }
        return null
    }

    // Bind callback functions
    fun bindPlayerGameData(
        channel: PresenceChannel,
        callback: (PlayerGameData) -> Unit
    ) {
        playerGameDataCallback =
            bindPresenceChannel(channel, PLAYER_UPDATE_DATA) { event: PusherEvent ->
                val gson = Gson()
                val data = gson.fromJson(event.data, PlayerGameData::class.java)
                if (event.userId == channel.me.id) return@bindPresenceChannel
                callback(data)
            }
    }

    
    fun bindPlayerLost(
        channel: PresenceChannel,
        callback: (PlayerGameData) -> Unit
    ) {
        playerLostCallback =
            bindPresenceChannel(channel, PLAYER_DECLARE_LOSS) { event: PusherEvent ->
                val gson = Gson()
                val data = gson.fromJson(event.data, PlayerGameData::class.java)
                callback(data)
            }
    }

    
    fun bindPause(
        channel: PresenceChannel,
        callback: () -> Unit
    ) {
        gamePauseCallback =
            bindPresenceChannel(channel, GAME_PAUSE) { callback() }
    }

    
    fun bindResume(
        channel: PresenceChannel,
        callback: () -> Unit
    ) {
        gameResumeCallback =
            bindPresenceChannel(channel, GAME_RESUME) { callback() }
    }

    
    fun bindGameStart(
        channel: PresenceChannel,
        callback: (GameStartedData) -> Unit
    ) {
        gameStartCallback = bindPresenceChannel(channel, GAME_START) { event: PusherEvent ->
            val gson = Gson()
            val data = gson.fromJson(event.data, GameStartedData::class.java)
            callback(data)
        }
    }

    
    fun bindGameOver(
        channel: PresenceChannel,
        callback: (GameOverData) -> Unit
    ) {
        gameOverCallback = bindPresenceChannel(channel, GAME_OVER) { event: PusherEvent ->
            val gson = Gson()
            val data = gson.fromJson(event.data, GameOverData::class.java)
            callback(data)
        }
    }

    
    fun unbindPlayerGameData(channel: PresenceChannel) {
        channel.unbind(PLAYER_UPDATE_DATA, playerGameDataCallback)
    }

    
    fun unbindPlayerLost(channel: PresenceChannel) {
        channel.unbind(PLAYER_DECLARE_LOSS, playerLostCallback)
    }

    
    fun unbindPause(channel: PresenceChannel) {
        channel.unbind(GAME_PAUSE, gamePauseCallback)
    }

    
    fun unbindResume(channel: PresenceChannel) {
        channel.unbind(GAME_RESUME, gameResumeCallback)
    }

    
    fun unbindGameStart(channel: PresenceChannel) {
        channel.unbind(GAME_START, gameStartCallback)
    }

    
    fun unbindGameOver(channel: PresenceChannel) {
        channel.unbind(GAME_OVER, gameOverCallback)
    }

    fun bindPresenceChannel(
        channel: PresenceChannel,
        eventName: String?,
        listener: SubscriptionEventListener
    ): PresenceChannelEventListener {
        val presenceListener = createEventListener(listener)
        channel.bind(eventName, presenceListener)
        return presenceListener
    }

    fun createEventListener(
        listener: SubscriptionEventListener
    ): PresenceChannelEventListener {
        return object : PresenceChannelEventListener {
            override fun onUsersInformationReceived(channelName: String, users: Set<User>) {}
            override fun userSubscribed(channelName: String, user: User) {}
            override fun userUnsubscribed(channelName: String, user: User) {}
            override fun onAuthenticationFailure(message: String, e: Exception) {}
            override fun onSubscriptionSucceeded(channelName: String) {}
            override fun onEvent(event: PusherEvent) {
                listener.onEvent(event)
            }
        }
    }

    @JvmOverloads
    fun createEventListener(
        onSubscribed: (channelName: String) -> Unit = { _ -> },
        onUserSubscribed: (channelName: String, user: User) -> Unit = { _, _ -> },
        onUserUnsubscribed: (channelName: String, user: User) -> Unit = { _, _ -> },
        onAuthenticationFailed: (message: String, e: Exception) -> Unit = { _, _ -> },
    ): PresenceChannelEventListener {
        return object : PresenceChannelEventListener {
            override fun onUsersInformationReceived(channelName: String, users: Set<User>) {
                for (user in users) {
                    userSubscribed(channelName, user)
                }
            }

            override fun userSubscribed(channelName: String, user: User) {
                onUserSubscribed(channelName, user)
            }

            override fun userUnsubscribed(channelName: String, user: User) {
                onUserUnsubscribed(channelName, user)
            }

            override fun onAuthenticationFailure(message: String, e: Exception) {
                onAuthenticationFailed(message, e)
            }

            override fun onSubscriptionSucceeded(channelName: String) {
                onSubscribed(channelName)
            }

            override fun onEvent(event: PusherEvent) {}
        }
    }

    fun getPresenceChannel(
        pusher: Pusher,
        channelName: String,
        listener: PresenceChannelEventListener
    ): PresenceChannel {
        return pusher.getPresenceChannel(channelName) ?: pusher.subscribePresence(channelName, listener)
    }
}