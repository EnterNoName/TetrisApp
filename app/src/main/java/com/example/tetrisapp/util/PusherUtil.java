package com.example.tetrisapp.util;

import android.util.Log;

import androidx.navigation.Navigation;

import com.example.tetrisapp.R;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PusherUtil {
    public final static String TAG = "Pusher";
    private static Pusher pusherInstance = null;
    private static Map<String, PresenceChannel> presenceChannelMap = new HashMap<>();

    public static Pusher getPusherInstance(String idToken, String authenticationEndpoint) {
        if (pusherInstance != null) return pusherInstance;

        PusherOptions options = new PusherOptions();
        options.setCluster("ap1");

        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("idToken", idToken);

        options.setChannelAuthorizer(new HttpChannelAuthorizer(authenticationEndpoint, new UrlEncodedConnectionFactory(bodyMap)));

        Pusher pusher = new Pusher("754b90f0401e477f519a", options);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i(TAG, "State changed from " + change.getPreviousState() +
                        " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.i(TAG, "There was a problem connecting! " +
                        "\ncode: " + code +
                        "\nmessage: " + message +
                        "\nException: " + e
                );
            }
        }, ConnectionState.ALL);

        pusherInstance = pusher;
        return pusher;
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

    public static void unsubscribePresence() {
        presenceChannelMap.keySet().forEach(val -> {
            pusherInstance.unsubscribe(val);
        });
    }

    public static PresenceChannel getPresenceChannel(
            String channelName
    ) {
        if (pusherInstance == null) return null;

        return presenceChannelMap.getOrDefault(channelName, null);
    }

    public static PresenceChannel getPresenceChannel(
            String channelName,
            UserActionEvent userSubscribedListener,
            UserActionEvent userUnsubscribedListener,
            AuthenticationFailedEvent authenticationFailedEvent
    ) {
        if (getPresenceChannel(channelName) != null) return getPresenceChannel(channelName);

        presenceChannelMap.put(channelName, pusherInstance.subscribePresence(channelName, new PresenceChannelEventListener() {
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
        }));

        return presenceChannelMap.get(channelName);
    }

    public interface UserActionEvent {
        void call(final String channelName, final User user);
    }

    public interface AuthenticationFailedEvent {
        void call(final String message, final Exception e);
    }
}
