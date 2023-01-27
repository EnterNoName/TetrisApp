package com.example.tetrisapp.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.tetrisapp.model.local.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class LobbyViewModel extends ViewModel {
    private final List<UserInfo> userList = new ArrayList<>();
    private String idToken = "";
    private String lobbyOwnerName;

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public void setLobbyOwnerName(String lobbyOwnerName) {
        this.lobbyOwnerName = lobbyOwnerName;
    }

    public List<UserInfo> getUserList() {
        return userList;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getLobbyOwnerName() {
        return lobbyOwnerName;
    }
}
