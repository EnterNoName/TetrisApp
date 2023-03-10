package com.example.tetrisapp.model.game

import com.example.tetrisapp.data.local.TetrominoRandomizer
import com.example.tetrisapp.interfaces.PieceConfiguration
import com.example.tetrisapp.interfaces.PlayfieldInterface
import com.example.tetrisapp.interfaces.TetrisInterface
import com.example.tetrisapp.util.MathUtil
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.math.ceil
import kotlin.math.max

class Tetris(
    configuration: PieceConfiguration,
    starterPieces: Array<String>,
    initialHistory: Array<String>
) : TetrisInterface {
    var onGameValuesUpdateCallback: () -> Unit = {}
    var onMoveCallback: () -> Unit = {}
    var onPauseCallback: () -> Unit = {}
    var onResumeCallback: () -> Unit = {}
    var onGameOverCallback: () -> Unit = {}
    var onLineClearCallback: () -> Unit = {}
    var onSolidifyCallback: () -> Unit = {}
    var onHoldCallback: () -> Unit = {}
    var onHardDropCallback: () -> Unit = {}

    // In-Game values
    private val randomizer: TetrominoRandomizer
    override var currentPiece: Piece? = null
    override var shadow: Piece? = null
    override var heldPiece: String? = null
    override lateinit var configuration: PieceConfiguration
    override var playfield: PlayfieldInterface = Playfield()
    override var tetrominoSequence: LinkedList<String> = LinkedList<String>()
    override val delay: Long
        get() { return future?.getDelay(TimeUnit.MILLISECONDS) ?: 0 }
    override var score = 0
    override var lines = 0
    override var level = 0
    override var combo = 0
    override var isLocking = false
    var speed = DEFAULT_SPEED
        private set
    var isGameOver = false
        private set
    var isPaused = true
        private set
    var delayLeft = 0L
        private set
    private var softDrop = false
    private var isHoldUsed = false

    private val nextTetromino: Piece
        get() {
            if (tetrominoSequence.size < GENERATE_AHEAD + 1) {
                generateSequence()
            }
            val name = tetrominoSequence.remove()
            val piece = configuration[name].copy()
            val col = (playfield.state[0].size / 2 - ceil(piece.matrix.size / 2f)).toInt()
            piece.col = col
            return piece
        }

    init {
        randomizer = TetrominoRandomizer(configuration.names, starterPieces, initialHistory)
        this.configuration = configuration
        currentPiece = nextTetromino
        calculateShadow()
        onGameValuesUpdateCallback()
    }

    private var future: ScheduledFuture<*>? = null

    private fun startLooper(delay: Long, speedMultiplier: Float) {
        stopLooper()
        future = Executors
            .newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate({
                if (!isGameOver && !isPaused) {
                    moveTetrominoDown()
                } else {
                    stopLooper()
                }
            },
            delay,
            (max(speed, MIN_SPEED) * speedMultiplier).toLong(),
            TimeUnit.MILLISECONDS)
    }

    private fun stopLooper() {
        future?.cancel(true)
        future = null
    }

    fun calculateShadow() {
        var yOffset = 0
        while (playfield.isValidMove(
                currentPiece!!.matrix,
                currentPiece!!.row + yOffset + 1,
                currentPiece!!.col
            )
        ) {
            yOffset++
        }
        shadow = currentPiece!!.copy()
        shadow!!.row = shadow!!.row + yOffset
    }

    private fun generateSequence() {
        while (tetrominoSequence.size < GENERATE_AHEAD * 2 + 1) {
            tetrominoSequence.add(randomizer.next())
        }
    }

    private fun placeTetromino() {
        for (row in currentPiece!!.matrix.indices) {
            for (col in currentPiece!!.matrix[row].indices) {
                if (currentPiece!!.matrix[row][col].toInt() == 1) {
                    if (currentPiece!!.row + row < 2) {
                        if (!isGameOver) {
                            isGameOver = true
                            onGameOverCallback()
                        }
                        return
                    }
                    playfield.state[currentPiece!!.row + row][currentPiece!!.col + col] = currentPiece!!.name
                }
            }
        }
        currentPiece = nextTetromino
        val linesCleared = clearLines()
        calculateShadow()
        onSolidifyCallback()
        updateGameValues(linesCleared)
        startLooper(0L, 1f)
        softDrop = false
        isHoldUsed = false
        onGameValuesUpdateCallback()
    }

    private fun updateGameValues(linesCleared: Int) {
        if (linesCleared == 0) {
            combo = 0
            return
        }
        lines += linesCleared
        level = lines / 10
        score += when (linesCleared) {
            1 -> (40 + 10 * combo) * (level + 1)
            2 -> (100 + 10 * combo) * (level + 1)
            3 -> (300 + 10 * combo) * (level + 1)
            4 -> (1200 + 10 * combo) * (level + 1)
            else -> (300 + 10 * combo) * linesCleared * (level + 1)
        }
        combo += linesCleared
        speed = DEFAULT_SPEED - level * 50
        onLineClearCallback()
    }

    private fun clearLines(): Int {
        var clearLinesTotal = 0
        var row = playfield.state.size - 1
        while (row >= 2) {
            var sendLinesCount = 0
            var clearLinesCount = 0
            if (playfield.state[row].all { it != null }) {
                if (playfield.state[row].any { it == "XXX" }) sendLinesCount++

                do {
                    playfield.state[row - clearLinesCount] = arrayOfNulls(10) // Clear the column
                    clearLinesCount++
                } while (playfield.state[row - clearLinesCount].all { it != null })

                val playfieldStateCopy = playfield.state.map { it.copyOf() }.toTypedArray()
                val positionsList = ArrayList<ArrayList<Pair<Int, Int>>>()
                for (y in row - clearLinesCount downTo 2) {
                    for (x in playfield.state[y].indices) {
                        if (playfield.state[y][x] != null) {
                            val positions = ArrayList<Pair<Int, Int>>()
                            MathUtil.floodFill(
                                playfield.state,
                                y,
                                x,
                                { pos: String? -> pos != null }) { _: Array<Array<String?>>, i: Int, j: Int ->
                                positions.add(Pair(i, j))
                                null
                            }
                            positionsList.add(positions)
                        }
                    }
                }
                var yOffset = 1
                while (positionsList.isNotEmpty()) {
                    val finalYOffset = yOffset + clearLinesCount
                    var i = 0
                    while (i < positionsList.size) {
                        val pos = positionsList[i]
                        if (pos.stream().anyMatch { (first, x): Pair<Int, Int> ->
                                val y = first + finalYOffset
                                y >= playfield.state.size || playfield.state[y][x] != null
                            }) {
                            positionsList.removeAt(i)
                            pos.forEach(Consumer { (yInitial, x): Pair<Int, Int> ->
                                val y = yInitial + finalYOffset - 1
                                playfield.state[y][x] = playfieldStateCopy[yInitial][x]
                            })
                        } else {
                            i++
                        }
                    }
                    yOffset++
                }
                row += yOffset - 1
            }
            clearLinesTotal += (clearLinesCount - sendLinesCount)
            row--
        }
        return clearLinesTotal
    }

    // Controls
    @Synchronized
    fun moveTetrominoRight() {
        if (isPaused || isGameOver) return

        if (playfield.isValidMove(currentPiece!!.matrix, currentPiece!!.row, currentPiece!!.col + 1)) {
            currentPiece!!.col = currentPiece!!.col + 1
            onMoveCallback()
            calculateShadow()
        }
    }

    @Synchronized
    fun moveTetrominoLeft() {
        if (isPaused || isGameOver) return

        if (playfield.isValidMove(currentPiece!!.matrix, currentPiece!!.row, currentPiece!!.col - 1)) {
            currentPiece!!.col = currentPiece!!.col - 1
            onMoveCallback()
            calculateShadow()
        }
    }

    @Synchronized
    fun rotateTetrominoRight() {
        if (isPaused) return

        val rotatedMatrix = MathUtil.rotateMatrixClockwise(currentPiece!!.matrix.map { it.copyOf() }.toTypedArray())
        if (playfield.isValidMove(rotatedMatrix, currentPiece!!.row, currentPiece!!.col)) {
            currentPiece!!.matrix = rotatedMatrix
            onMoveCallback()
            calculateShadow()
        }
    }

    @Synchronized
    fun rotateTetrominoLeft() {
        if (isPaused || isGameOver) return

        val rotatedMatrix = MathUtil.rotateMatrixCounterclockwise(currentPiece!!.matrix.map { it.copyOf() }.toTypedArray())
        if (playfield.isValidMove(rotatedMatrix, currentPiece!!.row, currentPiece!!.col)) {
            currentPiece?.matrix = rotatedMatrix
            onMoveCallback()
            calculateShadow()
        }
    }

    @Synchronized
    private fun moveTetrominoDown() {
        if (isPaused || isGameOver) return
        if (playfield.isValidMove(currentPiece!!.matrix, currentPiece!!.row + 1, currentPiece!!.col)) {
            currentPiece!!.row = currentPiece!!.row + 1
            onMoveCallback()
            isLocking = false
        } else {
            isLocking = if (!isLocking) {
                startLooper(LOCK_DELAY, 1f)
                true
            } else {
                placeTetromino()
                false
            }
        }
    }

    @Synchronized
    fun hardDrop() {
        if (isPaused || isGameOver) return
        stopLooper()
        while (playfield.isValidMove(currentPiece!!.matrix, currentPiece!!.row + 1, currentPiece!!.col)) {
            currentPiece!!.row = currentPiece!!.row + 1
        }
        placeTetromino()
        onHardDropCallback()
    }

    fun hold() {
        if (isPaused || isGameOver) return

        if (!isHoldUsed) {
            if (heldPiece == null) {
                heldPiece = currentPiece!!.name
                currentPiece = nextTetromino
            } else {
                val temp = configuration[heldPiece!!].copy()
                heldPiece = currentPiece!!.name
                currentPiece = temp
                val col =
                    (playfield.state[0].size / 2 - ceil(currentPiece!!.matrix.size / 2f)).toInt()
                currentPiece!!.col = col
            }
            calculateShadow()
            onHoldCallback()
            isHoldUsed = true
        }
    }

    fun stop() {
        isGameOver = true
    }

    // Getters
    fun isSoftDrop(): Boolean {
        return softDrop
    }

    // Setters
    fun setPause(pause: Boolean) {
        if (pause == isPaused || isGameOver) return

        if (pause) {
            if (future == null) return
            delayLeft = future?.getDelay(TimeUnit.MILLISECONDS) ?: 0
            stopLooper()
            onPauseCallback()
        } else {
            startLooper(delayLeft, 1f)
            delayLeft = 0
            onResumeCallback()
        }
        isPaused = pause
    }

    fun setSoftDrop(softDrop: Boolean) {
        if (softDrop == this.softDrop || isPaused || isGameOver) return

        this.softDrop = softDrop
        delayLeft = future?.getDelay(TimeUnit.MILLISECONDS) ?: 0
        if (softDrop) {
            startLooper(delayLeft / 4L, 0.25f)
        } else {
            startLooper(delayLeft * 4L, 1f)
        }
    }

    companion object {
        const val GENERATE_AHEAD = 4
        const val DEFAULT_SPEED = 750L
        const val MIN_SPEED = 125L
        const val LOCK_DELAY = 500L
    }
}