package com.example.tetrisapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetrisapp.data.local.dao.LeaderboardDao
import com.example.tetrisapp.model.local.LeaderboardEntry
import com.example.tetrisapp.model.remote.response.PublicRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScoresViewModel @Inject constructor(
    val leaderboardDao: LeaderboardDao
): ViewModel() {
    private val _uiState: MutableStateFlow<ScoresListAction> = MutableStateFlow(ScoresListAction.Load(emptyList()))
    val uiState: StateFlow<ScoresListAction> = _uiState

    val scores: MutableList<PublicRecord> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            leaderboardDao.getSorted()?.let { scores ->
                withContext(Dispatchers.Main) {
                    _uiState.value = ScoresListAction.Load(scores)
                }
            }
        }
    }

    fun deleteScore(pos: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            leaderboardDao.delete(_uiState.value.data[pos])
            withContext(Dispatchers.Main) {
                _uiState.value = ScoresListAction.Delete(
                    _uiState.value.data.toMutableList().also { it.removeAt(pos) },
                    pos
                )
            }
        }
    }

    sealed class ScoresListAction(
        open val data: List<LeaderboardEntry>
    ) {
        data class Delete(override val data: List<LeaderboardEntry>, val pos: Int): ScoresListAction(data)
        data class Insert(override val data: List<LeaderboardEntry>): ScoresListAction(data)
        data class Update(override val data: List<LeaderboardEntry>): ScoresListAction(data)
        data class Load(override val data: List<LeaderboardEntry>): ScoresListAction(data)
    }
}