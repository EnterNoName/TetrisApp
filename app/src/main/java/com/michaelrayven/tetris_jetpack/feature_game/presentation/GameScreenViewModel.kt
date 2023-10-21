package com.michaelrayven.tetris_jetpack.feature_game.presentation

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaelrayven.tetris_jetpack.feature_game.domain.model.GameInfo
import com.michaelrayven.tetris_jetpack.feature_game.domain.use_case.GameUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameScreenViewModel @Inject constructor(
    private val useCases: GameUseCases
): ViewModel() {
    private val _state = mutableStateOf(GameInfo())
    val state: State<GameInfo> = _state

    init {
        viewModelScope.launch {
            useCases.GetGameState().collectLatest {
                _state.value = it
            }
        }
        useCases.StartGame()
    }

    fun onSwipeDown() {
        useCases.SoftDropTetromino()
    }

    fun onHoldPressed() {
        useCases.HoldTetromino()
    }

    fun onDoubleTap() {
        useCases.HardDropTetromino()
    }

    fun onTapRight() {
        useCases.RotateTetrominoCounterClockwise()
    }

    fun onTapLeft() {
        useCases.RotateTetrominoClockwise()
    }

    fun onDragRight() {
        useCases.MoveTetrominoRight()
    }

    fun onDragLeft() {
        useCases.MoveTetrominoLeft()
    }

    suspend fun saveScore(): Long {
        return useCases.InsertScore(
            score = state.value.score,
            lines = state.value.lines,
            level = state.value.level,
            gameTime = state.value.gameTime,
            date = state.value.date
        )
    }

    companion object {
        const val MOVEMENT_THRESHOLD = 20f
        const val DRAG_THRESHOLD = 30f
        const val VELOCITY_THRESHOLD = 8f
    }
}