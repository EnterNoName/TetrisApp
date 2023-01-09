package com.example.tetrisapp.util;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import java.net.InetAddress;

public class ConnectionUtil {
    private final Activity activity;
    NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build();
    Callback onAvailable;
    Callback onLost;

    public ConnectionUtil(Activity activity) {
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

    public void checkInternetConnection(Callback onAvailable) {
        checkInternetConnection(onAvailable, () -> {});
    }

    public void checkInternetConnection(Callback onAvailable, Callback onLost) {
        this.onAvailable = onAvailable;
        this.onLost = onLost;

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (isInternetAvailable()) {
                    onAvailable.call();
                } else {
                    onLost.call();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                onLost.call();
            }
        };

        ConnectivityManager connectivityManager =
                activity.getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);
    }

    public interface Callback {
        void call();
    }
}
