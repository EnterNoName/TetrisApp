package com.example.tetrisapp.interfaces;

import com.pusher.client.channel.User;

public interface FindUserCallback {
    boolean call(User user);
}
