package com.example.tetrisapp.util;

import android.util.Log;

import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.channel.User;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpChannelAuthorizer;
import com.pusher.client.util.UrlEncodedConnectionFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class PusherUtil {
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

    public static PresenceChannel getPresenceChannel(
            Pusher pusher,
            String channelName,
            UserActionEvent userSubscribedListener,
            UserActionEvent userUnsubscribedListener,
            AuthenticationFailedEvent authenticationFailedEvent
    ) {
        if (pusher.getPresenceChannel(channelName) == null) {
            pusher.subscribePresence(channelName, new PresenceChannelEventListener() {
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
            });
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
