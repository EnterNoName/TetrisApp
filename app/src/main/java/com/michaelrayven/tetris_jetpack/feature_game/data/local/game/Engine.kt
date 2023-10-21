package com.michaelrayven.tetris_jetpack.feature_game.data.local.game

import com.michaelrayven.tetris_jetpack.core.util.deepCopy
import com.michaelrayven.tetris_jetpack.core.util.rotateMatrixClockwise
import com.michaelrayven.tetris_jetpack.core.util.rotateMatrixCounterclockwise
import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.tetromino_sets.TetrominoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.math.ceil

class Engine(
    private val playfield: Playfield,
    val tetrominoSet: TetrominoSet
) {
    // Game Logic Variables
    private var gameClock: Timer? = null
    private val sequenceGenerator: SequenceGenerator
    private lateinit var currentTetromino: Tetromino
    private var nextTetrominoes: Array<String>
    private var shadowYOffset: Int = 0
    private var heldTetromino: String? = null

    // Game Progression Variables
    private var score: Int = 0
    private var level: Int = 0
    private var lines: Int = 0
    private var combo: Int = 0
    private var speed: Long = NORMAL_SPEED
    private var isGameOver: Boolean = false
    private var isPaused: Boolean = false

    // Helper Values
    private var isHoldAvailable: Boolean = true
    private var isLocking: Boolean = false
    private var isSoftDropActive: Boolean = false
    private var remainingDelay: Long = 0

    // Statistical Variables
    private val date: Long = Instant.now().toEpochMilli()
    private var gameTime: Long = 0

    // Communications
    val action = MutableSharedFlow<GameAction>(1, 1)

    private val _state = MutableSharedFlow<GameState>(1, 1)
    val state = _state.asSharedFlow()

    init {
        sequenceGenerator = SequenceGenerator(
            tetrominoes = tetrominoSet.names,
            possibleFirstTetrominoes = tetrominoSet.possibleFirstTetrominoes,
            initialHistory = tetrominoSet.initialHistory
        )
        // If this fails it means that
        // SequenceGenerator gave a non-existing tetromino name
        setCurrentTetromino(sequenceGenerator.next(), -3)
        nextTetrominoes = Array(NEXT_TETROMINOES_COUNT) { sequenceGenerator.next() }
        calculateShadowYOffset()

        CoroutineScope(Dispatchers.Main).launch {
            action.collectLatest { handleAction(it) }
        }
    }

    private fun handleAction(action: GameAction) {
        if (isGameOver || isPaused) return

        var newShapeMatrix = currentTetromino.shapeMatrix.deepCopy()
        var offsetX = 0
        var offsetY = 0

        when (action) {
            is GameAction.MoveDown -> {
                offsetY++
            }

            is GameAction.MoveLeft -> {
                offsetX--
            }

            is GameAction.MoveRight -> {
                offsetX++
            }

            is GameAction.SoftDropDown -> {
                // TODO: Fix inaccurate calculations
                if (isSoftDropActive) {
                    speed *= SOFT_DROP_SPEED_MULT
                    remainingDelay *= SOFT_DROP_SPEED_MULT
                } else {
                    speed /= SOFT_DROP_SPEED_MULT
                    remainingDelay /= SOFT_DROP_SPEED_MULT
                }

                isSoftDropActive = !isSoftDropActive
            }
            is GameAction.HardDropDown -> {
                for (i in currentTetromino.row .. playfield.height) {
                    if (!playfield.isValidMove(
                            shapeMatrix = newShapeMatrix,
                            x = currentTetromino.col,
                            y = i
                        )
                    ) {
                        offsetY = i - 1 - currentTetromino.row
                        break
                    }
                }
            }
            is GameAction.RotateClockwise -> {
                newShapeMatrix = rotateMatrixClockwise(newShapeMatrix)
            }
            is GameAction.RotateCounterClockwise -> {
                newShapeMatrix = rotateMatrixCounterclockwise(newShapeMatrix)
            }
            is GameAction.Hold -> {
                if (isHoldAvailable) {
                    val currentTetrominoName = currentTetromino.name
                    if (heldTetromino == null) {
                        getNextTetromino()
                    } else {
                        setCurrentTetromino(heldTetromino!!, -2)
                    }
                    heldTetromino = currentTetrominoName
                }

                isHoldAvailable = false

                calculateShadowYOffset()
                emitState()

                return
            }
        }

        if (playfield.isValidMove(
                shapeMatrix = newShapeMatrix,
                x = currentTetromino.col + offsetX,
                y = currentTetromino.row + offsetY
            )
        ) {
            isLocking = false

            currentTetromino = currentTetromino.copy(
                shapeMatrix = newShapeMatrix,
                col = currentTetromino.col + offsetX,
                row = currentTetromino.row + offsetY
            )

            if (action is GameAction.HardDropDown) {
                if (playfield.bindTetromino(currentTetromino)) {
                    val linesCleared = playfield.clearLines()
                    calculateGameValues(linesCleared)
                    getNextTetromino()
                    isHoldAvailable = true
                } else {
                    handleGameOver()
                }
            }
        } else if (action is GameAction.MoveDown) {
            if (isLocking) {
                isLocking = false

                if (playfield.bindTetromino(currentTetromino)) {
                    val linesCleared = playfield.clearLines()
                    calculateGameValues(linesCleared)
                    getNextTetromino()
                    isHoldAvailable = true
                } else {
                    handleGameOver()
                }
            } else {
                isLocking = true

                startGameClock(LOCK_DELAY)
                remainingDelay = 0
            }
        }

        calculateShadowYOffset()
        emitState()
    }

    private fun emitState() {
        _state.tryEmit(
            GameState(
                playfield = playfield,
                currentTetromino = currentTetromino,
                nextTetrominoes = nextTetrominoes,
                shadowYOffset = shadowYOffset,
                heldTetromino = heldTetromino,
                score = score,
                level = level,
                lines = lines,
                combo = combo,
                isGameOver = isGameOver,
                date = date,
                gameTime = gameTime
            )
        )
    }

    private fun getNextTetromino() {
        setCurrentTetromino(nextTetrominoes[0], -2)
        nextTetrominoes = nextTetrominoes.drop(1).plus(sequenceGenerator.next()).toTypedArray()
    }

    private fun setCurrentTetromino(name: String, row: Int) {
        val tetromino = tetrominoSet[name]!!
        val col = playfield.state.size / 2 - ceil(tetromino.shapeMatrix.size / 2f)
        currentTetromino = tetromino.copy(col = col.toInt(), row = row)
    }

    private fun handleGameOver() {
        isGameOver = true
        gameClock?.cancel()
    }

    private fun calculateShadowYOffset() {
        // Maximum possible Y offset within playfield bounds
        for (i in currentTetromino.row .. playfield.height) {
            if (!playfield.isValidMove(
                    shapeMatrix = currentTetromino.shapeMatrix,
                    x = currentTetromino.col,
                    y = i
                )
            ) {
                shadowYOffset = i - 1
                return
            }
        }
    }

    private fun calculateGameValues(linesCleared: Int) {
        combo = if (linesCleared > 0) combo + linesCleared else 0
        lines += linesCleared
        level = lines / 10
        score += when (linesCleared) {
            1 -> (40 + 10 * combo) * (level + 1)
            2 -> (100 + 10 * combo) * (level + 1)
            3 -> (300 + 10 * combo) * (level + 1)
            4 -> (1200 + 10 * combo) * (level + 1)
            else -> (300 + 10 * combo) * linesCleared * (level + 1)
        }
        speed = NORMAL_SPEED - level * 50
    }

    private fun gameTick() {
        action.tryEmit(GameAction.MoveDown)
    }

    fun startGameClock(delay: Long = 0) {
        if (isGameOver) return

        gameClock?.cancel()
        gameClock = fixedRateTimer(
            name = "Game Clock",
            daemon = true,
            initialDelay = delay,
            period = 1L,
            action = {
                if (remainingDelay == 0L) {
                    remainingDelay = speed.coerceAtLeast(MIN_SPEED)
                    gameTick()
                } else {
                    remainingDelay--
                }

                gameTime++
            }
        )
    }

    companion object {
        const val NEXT_TETROMINOES_COUNT = 5
        const val SOFT_DROP_SPEED_MULT = 4
        const val LOCK_DELAY = 500L
        const val CLEAR_LINE_DELAY = 500L
        const val NORMAL_SPEED = 1000L
        const val MIN_SPEED = 100L
    }
}