package com.example.tetrisapp.util;

import com.example.tetrisapp.interfaces.Callback;
import com.example.tetrisapp.interfaces.FindUserCallback;
import com.example.tetrisapp.interfaces.GameOverCallback;
import com.example.tetrisapp.interfaces.GameStartedCallback;
import com.example.tetrisapp.interfaces.PlayerGameDataCallback;
import com.example.tetrisapp.interfaces.PlayerLostCallback;
import com.example.tetrisapp.model.local.model.GameOverData;
import com.example.tetrisapp.model.local.model.GameStartedData;
import com.example.tetrisapp.model.local.model.PlayerLostData;
import com.example.tetrisapp.model.local.model.PlayerGameData;
import com.example.tetrisapp.model.local.model.UserInfo;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.channel.User;

import java.util.Set;

public class PusherUtil {
    public static final String PLAYER_UPDATE_DATA = "client-player-update-data";
    public static final String PLAYER_DECLARE_LOSS = "client-player-declare-loss";
    public static final String GAME_PAUSE = "client-game-pause";
    public static final String GAME_RESUME = "client-game-resume";
    public static final String GAME_START = "game-started";
    public static final String GAME_OVER = "game-ended";

    private static SubscriptionEventListener playerGameDataCallback;
    private static SubscriptionEventListener playerLostCallback;
    private static SubscriptionEventListener gameStartCallback;
    private static SubscriptionEventListener gameOverCallback;
    private static SubscriptionEventListener gamePauseCallback;
    private static SubscriptionEventListener gameResumeCallback;

    public static User findUser(PresenceChannel channel, FindUserCallback callback) {
        for (User user : channel.getUsers()) {
            if (callback.call(user))  {
                return user;
            }
        }

        return null;
    }

    public static UserInfo getUserInfo(PresenceChannel channel, String userId) {
        Gson gson = new Gson();

        if (channel.getMe().getId().equals(userId)) {
            return gson.fromJson(channel.getMe().getInfo(), UserInfo.class);
        }

        for (User user : channel.getUsers()) {
            if (user.getId().equals(userId))  {
                return gson.fromJson(user.getInfo(), UserInfo.class);
            }
        }

        return null;
    }

    // Bind callback functions

    public static void bindPlayerGameData(
            PresenceChannel channel,
            PlayerGameDataCallback callback
    ) {
        playerGameDataCallback = bindPresenceChannel(channel, PLAYER_UPDATE_DATA, event -> {
            Gson gson = new Gson();
            PlayerGameData data = gson.fromJson(event.getData(), PlayerGameData.class);

            if (event.getUserId().equals(channel.getMe().getId())) return;

            callback.call(data);
        });
    }

    public static void bindPlayerLost(
            PresenceChannel channel,
            PlayerLostCallback callback
    ) {
        playerLostCallback = bindPresenceChannel(channel, PLAYER_DECLARE_LOSS, event -> {
            Gson gson = new Gson();
            PlayerGameData data = gson.fromJson(event.getData(), PlayerGameData.class);

            callback.call(data);
        });
    }

    public static void bindPause(
            PresenceChannel channel,
            Callback callback
    ) {
        gamePauseCallback = bindPresenceChannel(channel, GAME_PAUSE, event -> {
            callback.call();
        });
    }

    public static void bindResume(
            PresenceChannel channel,
            Callback callback
    ) {
        gameResumeCallback = bindPresenceChannel(channel, GAME_RESUME, event -> {
            callback.call();
        });
    }

    public static void bindGameStart(
            PresenceChannel channel,
            GameStartedCallback callback
    ) {
        gameStartCallback = bindPresenceChannel(channel, GAME_START, event -> {
            Gson gson = new Gson();
            GameStartedData data = gson.fromJson(event.getData(), GameStartedData.class);

            callback.call(data);
        });
    }

    public static void bindGameOver(
            PresenceChannel channel,
            GameOverCallback callback
    ) {
        gameOverCallback = bindPresenceChannel(channel, GAME_OVER, event -> {
            Gson gson = new Gson();
            GameOverData data = gson.fromJson(event.getData(), GameOverData.class);

            callback.call(data);
        });
    }

    public static void unbindPlayerGameData(PresenceChannel channel) {
        channel.unbind(PLAYER_UPDATE_DATA, playerGameDataCallback);
    }

    public static void unbindPlayerLost(PresenceChannel channel) {
        channel.unbind(PLAYER_DECLARE_LOSS, playerLostCallback);
    }

    public static void unbindPause(PresenceChannel channel) {
        channel.unbind(GAME_PAUSE, gamePauseCallback);
    }

    public static void unbindResume(PresenceChannel channel) {
        channel.unbind(GAME_RESUME, gameResumeCallback);
    }

    public static void unbindGameStart(PresenceChannel channel) {
        channel.unbind(GAME_START, gameStartCallback);
    }

    public static void unbindGameOver(PresenceChannel channel) {
        channel.unbind(GAME_OVER, gameOverCallback);
    }

    public static PresenceChannelEventListener bindPresenceChannel(
            PresenceChannel channel,
            String eventName,
            SubscriptionEventListener listener
    ) {
        PresenceChannelEventListener presenceListener = createEventListener(listener);
        channel.bind(eventName, presenceListener);
        return presenceListener;
    }

    public static PresenceChannelEventListener createEventListener(
            SubscriptionEventListener listener
    ) {
        return new PresenceChannelEventListener() {
            @Override
            public void onUsersInformationReceived(String channelName, Set<User> users) {
            }

            @Override
            public void userSubscribed(String channelName, User user) {
            }

            @Override
            public void userUnsubscribed(String channelName, User user) {
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {}

            @Override
            public void onEvent(PusherEvent event) {
                listener.onEvent(event);
            }
        };
    }

    public static PresenceChannelEventListener createEventListener(
            UserActionEvent userSubscribedListener,
            UserActionEvent userUnsubscribedListener,
            AuthenticationFailedEvent authenticationFailedEvent
    ) {
        return createEventListener(userSubscribedListener, userUnsubscribedListener, authenticationFailedEvent, null);
    }

    public static PresenceChannelEventListener createEventListener(
            UserActionEvent userSubscribedListener,
            UserActionEvent userUnsubscribedListener,
            AuthenticationFailedEvent authenticationFailedEvent,
            Callback subscriptionSucceeded
    ) {
        return new PresenceChannelEventListener() {
            @Override
            public void onUsersInformationReceived(String channelName, Set<User> users) {
                for (User user : users) {
                    userSubscribed(channelName, user);
                }
            }

            @Override
            public void userSubscribed(String channelName, User user) {
                userSubscribedListener.call(channelName, user);
            }

            @Override
            public void userUnsubscribed(String channelName, User user) {
                userUnsubscribedListener.call(channelName, user);
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                authenticationFailedEvent.call(message, e);
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                if (subscriptionSucceeded == null) return;
                subscriptionSucceeded.call();
            }

            @Override
            public void onEvent(PusherEvent event) {}
        };
    }

    public static PresenceChannel getPresenceChannel(
            Pusher pusher,
            String channelName,
            PresenceChannelEventListener listener
    ) {
        if (pusher.getPresenceChannel(channelName) == null) {
            pusher.subscribePresence(channelName, listener);
        }

        return pusher.getPresenceChannel(channelName);
    }

    public interface UserActionEvent {
        void call(final String channelName, final User user);
    }

    public interface AuthenticationFailedEvent {
        void call(final String message, final Exception e);
    }
}
