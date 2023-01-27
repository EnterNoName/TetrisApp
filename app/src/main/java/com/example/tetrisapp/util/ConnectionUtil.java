package com.example.tetrisapp.util;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import java.net.InetAddress;

public class ConnectionUtil {
    ConnectivityManager connectivityManager;
    NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build();
    Callback onAvailable;
    Callback onLost;

    public ConnectionUtil(ConnectivityManager connectivityManager) {
        this.connectivityManager = connectivityManager;
    }

    public boolean isNetworkConnected() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) return false;

        return networkInfo.isConnected();
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddress = InetAddress.getByName("google.com");
            return !ipAddress.equals("");
        } catch (Exception e) {
            return false;
        }
    }

    public void checkInternetConnection() {
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

        connectivityManager.requestNetwork(networkRequest, networkCallback);
    }

    public void setOnAvailable(Callback onAvailable) {
        this.onAvailable = onAvailable;
    }

    public void setOnLost(Callback onLost) {
        this.onLost = onLost;
    }

    public interface Callback {
        void call();
    }
}
