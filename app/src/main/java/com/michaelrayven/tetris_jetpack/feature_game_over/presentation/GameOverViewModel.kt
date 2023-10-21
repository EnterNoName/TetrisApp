package com.michaelrayven.tetris_jetpack.feature_game_over.presentation

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.model.Score
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.use_case.GameOverUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameOverViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val useCases: GameOverUseCases
): ViewModel() {
    private val _state = mutableStateOf(GameOverScreenState())
    val state: State<GameOverScreenState> = _state

    init {
        val id: Long = checkNotNull(savedStateHandle["scoreId"])
        Log.d("Game Over View Model", id.toString())

        viewModelScope.launch {
            val currentScore = useCases.GetScoreById(id)
            val highScore = useCases.GetMaxScore()
            _state.value = GameOverScreenState(
                currentScore = Score(currentScore.score, currentScore.lines, currentScore.level),
                highScore = highScore,
                isNewHighScore =  currentScore.score > highScore.score
            )
        }
    }
}