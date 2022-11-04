package com.example.tetrisapp.util;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import java.net.InetAddress;

public class ConnectionHelper {
    private final Activity activity;
    private boolean hasInternetConnection = false;
    NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build();

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            hasInternetConnection = isInternetAvailable();
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            hasInternetConnection = false;
        }
    };

    public ConnectionHelper(Activity activity) {
        this.activity = activity;
    }

    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddress = InetAddress.getByName("google.com");
            return !ipAddress.equals("");
        } catch (Exception e) {
            return false;
        }
    }

    public void checkInternetConnection(Callback onAvailable, Callback onLost) {
        ConnectivityManager connectivityManager =
                activity.getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);

        if (hasInternetConnection) {
            onAvailable.call();
        } else {
            onLost.call();
        }
    }

    public interface Callback {
        void call();
    }
}
