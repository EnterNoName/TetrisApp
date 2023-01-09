package com.example.tetrisapp.util;

import android.util.Log;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpChannelAuthorizer;
import com.pusher.client.util.UrlEncodedConnectionFactory;

import java.util.HashMap;
import java.util.Map;

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

    public static void unsubscribePresence() {
        presenceChannelMap.keySet().forEach(val -> {
            pusherInstance.unsubscribe(val);
        });
    }

    public static PresenceChannel getPresenceChannel(Pusher pusherInstance, String channelName, PresenceChannelEventListener listener) {
        if (!presenceChannelMap.containsKey(channelName)) {
            presenceChannelMap.put(channelName, pusherInstance.subscribePresence(channelName, listener));
        }

        return presenceChannelMap.get(channelName);
    }
}
