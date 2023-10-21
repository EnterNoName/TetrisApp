package com.michaelrayven.tetris_jetpack.feature_game.data.repository

import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.Engine
import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.GameAction
import com.michaelrayven.tetris_jetpack.feature_game.domain.model.GameInfo
import com.michaelrayven.tetris_jetpack.feature_game.domain.repository.GameRepository
import com.michaelrayven.tetris_jetpack.feature_game_over.data.local.ScoreDao
import com.michaelrayven.tetris_jetpack.feature_game_over.domain.model.Score
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepositoryImpl(
    private val engine: Engine,
    private val scoreDao: ScoreDao
) : GameRepository {
    override suspend fun insertScore(score: Score, gameTime: Long, date: Long): Long {
        return scoreDao.insertScores(listOf(
            score.toScoreEntity(gameTime, date)
        ))[0]
    }

    override fun sendGameAction(action: GameAction) {
        engine.action.tryEmit(action)
    }

    override fun getGameState(): Flow<GameInfo> =
        engine.state.map { state ->
            val playfieldState = state.playfield.deepStateCopy()
            val shapeMatrix = state.currentTetromino.shapeMatrix

            // Insert the current tetromino shadow into playfield state
            for (col in shapeMatrix.indices) {
                for (row in shapeMatrix[col].indices) {
                    if (shapeMatrix[col][row] && state.shadowYOffset + row >= 0) {
                        playfieldState[state.currentTetromino.col + col][state.shadowYOffset + row] =
                            engine.tetrominoSet.shadowName
                    }
                }
            }

            // Insert the current tetromino into playfield state
            for (col in shapeMatrix.indices) {
                for (row in shapeMatrix[col].indices) {
                    if (shapeMatrix[col][row] && state.currentTetromino.row + row >= 0) {
                        playfieldState[state.currentTetromino.col + col][state.currentTetromino.row + row] =
                            state.currentTetromino.name
                    }
                }
            }

            return@map GameInfo(
                playfield = playfieldState,
                nextTetrominoes = state.nextTetrominoes,
                heldTetromino = state.heldTetromino,
                score = state.score,
                level = state.level,
                lines = state.lines,
                combo = state.combo,
                isGameOver = state.isGameOver,
                gameTime = state.gameTime,
                date = state.date
            )
        }

    override fun startGame() {
        engine.startGameClock(0)
    }
}