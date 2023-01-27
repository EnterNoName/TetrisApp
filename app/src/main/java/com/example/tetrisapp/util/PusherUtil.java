package com.example.tetrisapp.util;

import com.example.tetrisapp.interfaces.FindUserCallback;
import com.example.tetrisapp.interfaces.GameOverCallback;
import com.example.tetrisapp.interfaces.PlayerGameDataCallback;
import com.example.tetrisapp.interfaces.PlayerLostCallback;
import com.example.tetrisapp.model.local.model.GameOverData;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class PusherUtil {
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

        for (User user : channel.getUsers()) {
            if (user.getId().equals(userId))  {
                return gson.fromJson(user.getInfo(), UserInfo.class);
            }
        }

        return null;
    }

    public static void bindPlayerLost(
            PresenceChannel channel,
            PlayerLostCallback callback
    ) {
        bindPresenceChannel(channel, "user-lost", event -> {
            Gson gson = new Gson();
            PlayerLostData data = gson.fromJson(event.getData(), PlayerLostData.class);

            callback.call(data);
        });
    }

    public static void bindGameOver(
            PresenceChannel channel,
            GameOverCallback callback
    ) {
        bindPresenceChannel(channel, "game-ended", event -> {
            Gson gson = new Gson();
            GameOverData data = gson.fromJson(event.getData(), GameOverData.class);

            callback.call(data);
        });
    }

    public static void bindPlayerGameData(
            PresenceChannel channel,
            PlayerGameDataCallback callback
    ) {
        bindPresenceChannel(channel, "client-user-update-data", event -> {
            Gson gson = new Gson();
            PlayerGameData data = gson.fromJson(event.getData(), PlayerGameData.class);
            data.userId = event.getUserId();

            if (event.getUserId().equals(channel.getMe().getId())) return;

            callback.call(data);
        });
    }

    public static void bindPresenceChannel(
            PresenceChannel channel,
            String eventName,
            SubscriptionEventListener listener
    ) {
        channel.bind(eventName, new PresenceChannelEventListener() {
            @Override
            public void onUsersInformationReceived(String channelName, Set<User> users) {}

            @Override
            public void userSubscribed(String channelName, User user) {}

            @Override
            public void userUnsubscribed(String channelName, User user) {}

            @Override
            public void onAuthenticationFailure(String message, Exception e) {}

            @Override
            public void onSubscriptionSucceeded(String channelName) {}

            @Override
            public void onEvent(PusherEvent event) {
                listener.onEvent(event);
            }
        });
    }

    public static PresenceChannelEventListener createEventListener(
            UserActionEvent userSubscribedListener,
            UserActionEvent userUnsubscribedListener,
            AuthenticationFailedEvent authenticationFailedEvent
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
            public void onSubscriptionSucceeded(String channelName) {}

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

    public static PresenceChannel getPresenceChannel(
            Pusher pusher,
            String channelName,
            UserActionEvent userSubscribedListener,
            UserActionEvent userUnsubscribedListener,
            AuthenticationFailedEvent authenticationFailedEvent
    ) {
        if (pusher.getPresenceChannel(channelName) == null) {
            pusher.subscribePresence(channelName, createEventListener(userSubscribedListener, userUnsubscribedListener, authenticationFailedEvent));
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
