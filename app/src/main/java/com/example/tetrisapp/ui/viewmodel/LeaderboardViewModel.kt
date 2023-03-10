package com.example.tetrisapp.ui.viewmodel

import androidx.lifecycle.*
import com.example.tetrisapp.model.remote.response.PublicRecord

class LeaderboardViewModel : ViewModel() {
    var token: String? = null
    var page = 1
    var pageCount = 1

    private val mutableScores: MutableLiveData<List<PublicRecord>> by lazy {
        MutableLiveData(ArrayList())
    }
    val scores: LiveData<List<PublicRecord>> = mutableScores

    fun updateScores(list: List<PublicRecord>) {
        mutableScores.value = list
    }
}