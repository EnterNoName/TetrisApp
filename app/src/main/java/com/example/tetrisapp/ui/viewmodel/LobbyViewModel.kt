package com.example.tetrisapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetrisapp.model.local.model.UserInfo
import com.example.tetrisapp.util.FirebaseTokenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LobbyViewModel: ViewModel() {
    private val mutableUsersLiveData: MutableLiveData<List<UserInfo>> by lazy {
        MutableLiveData(ArrayList())
    }
    val usersLiveData: LiveData<List<UserInfo>> = mutableUsersLiveData

    var token: String? = null
    var lobbyOwnerName: String? = null

    init {
        FirebaseTokenUtil.getFirebaseToken { token: String? ->
            this.token = token
        }
    }

    fun addUser(user: UserInfo) {
        viewModelScope.launch(Dispatchers.Main) {
            mutableUsersLiveData.value = mutableUsersLiveData.value?.toMutableList()?.let {
                it.add(user)
                it
            }
        }
    }

    fun removeUser(user: UserInfo) {
        viewModelScope.launch(Dispatchers.Main) {
            mutableUsersLiveData.value = mutableUsersLiveData.value?.toMutableList()?.let {
                it.remove(user)
                it
            }
        }
    }
}