package com.example.tetrisapp.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.tetrisapp.R
import com.example.tetrisapp.interfaces.TetrisInterface
import com.example.tetrisapp.model.game.Tetris
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class GameView : View {
    private val paint = Paint()
    var game: TetrisInterface? = null
    private var pointSize = 0
    private var xOffset = 0
    private var yOffset = 0
    private val generalOffset = 20
    private var borderWidth = 5
    private var color = -0x1000000

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GameView,
            0, 0
        )
        try {
            color = a.getColor(R.styleable.GameView_backgroundColor, color)
            borderWidth = a.getInt(R.styleable.GameView_gridLineWidth, borderWidth)
        } finally {
            a.recycle()
        }
        init()
    }

    private fun init() {
        Executors
            .newSingleThreadScheduledExecutor()
            .scheduleWithFixedDelay(this::postInvalidate, 0, (1000 / FPS).toLong(), TimeUnit.MILLISECONDS)
    }

    private fun calculateDimensions(width: Int, height: Int) {
        pointSize =
            ((height - generalOffset * 2) / 20).coerceAtMost((width - generalOffset * 2) / 10)
        xOffset = (width - pointSize * 10) / 2
        yOffset = (height - pointSize * 20) / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (game != null) {
            canvas.drawColor(color)
            drawGrid(canvas)
            drawPlayfield(canvas)
            drawShadow(canvas)
            drawTetromino(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateDimensions(w, h)
    }

    private fun drawShadow(canvas: Canvas) {
        val tetromino = game!!.shadow ?: return
        for (y in tetromino.matrix.indices) {
            for (x in tetromino.matrix[y].indices) {
                if ((tetromino.matrix[y][x].toInt() == 1) and (tetromino.row + y - 2 >= 0)) {
                    drawPoint(
                        tetromino.col + x,
                        tetromino.row + y - 2,
                        SHADOW_COLOR,
                        tetromino.overlayResId,
                        canvas
                    )
                }
            }
        }
    }

    private fun drawGrid(canvas: Canvas) {
        paint.color = -0x99999a
        paint.strokeWidth = borderWidth.toFloat()
        paint.strokeCap = Paint.Cap.ROUND
        for (x in 0..10) {
            canvas.drawLine(
                (pointSize * x + xOffset).toFloat(),
                yOffset.toFloat(),
                (
                        pointSize * x + xOffset).toFloat(),
                (
                        height - yOffset).toFloat(),
                paint
            )
        }
        for (y in 0..20) {
            canvas.drawLine(
                xOffset.toFloat(),
                (
                        pointSize * y + yOffset).toFloat(),
                (
                        width - xOffset).toFloat(),
                (
                        pointSize * y + yOffset).toFloat(),
                paint
            )
        }
        paint.strokeCap = Paint.Cap.SQUARE
    }

    private fun drawPoint(x: Int, y: Int, color: Int, overlayResource: Int, canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 0f
        paint.color = color
        val point = Rect(
            x * pointSize + xOffset,
            y * pointSize + yOffset,
            (x + 1) * pointSize + xOffset,
            (y + 1) * pointSize + yOffset
        )
        canvas.drawRect(point, paint)
        val overlay = VectorDrawableCompat.create(context.resources, overlayResource, null)!!
        overlay.mutate()
        overlay.setBounds(0, 0, pointSize, pointSize)
        canvas.translate((x * pointSize + xOffset).toFloat(), (y * pointSize + yOffset).toFloat())
        overlay.draw(canvas)
        canvas.translate(-(x * pointSize + xOffset).toFloat(), -(y * pointSize + yOffset).toFloat())
    }

    private fun drawPlayfield(canvas: Canvas) {
        game?.playfield?.let {
            for (y in 2..21) {
                for (x in 0..9) {
                    val point = it.state[y][x]
                    if (point != null) {
                        game!!.configuration[point].let { piece ->
                            drawPoint(x, y - 2, piece.color, piece.overlayResId, canvas)
                        }
                    }
                }
            }
        }
    }

    private fun drawTetromino(canvas: Canvas) {
        val tetromino = game!!.currentPiece ?: return
        var tetrominoColor = tetromino.color
        val delay = game!!.delay
        if (game!!.isLocking && delay > 0) {
            val x = delay.toDouble() / Tetris.LOCK_DELAY
            tetrominoColor = ColorUtils.blendARGB(
                tetrominoColor,
                -0x1, (-2 * x.pow(2) + 2 * x + 0.5f).toFloat()
            )
        }
        for (y in tetromino.matrix.indices) {
            for (x in tetromino.matrix[y].indices) {
                if ((tetromino.matrix[y][x].toInt() == 1) and (tetromino.row + y - 2 >= 0)) {
                    drawPoint(
                        tetromino.col + x,
                        tetromino.row + y - 2,
                        tetrominoColor,
                        tetromino.overlayResId,
                        canvas
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "GameView"
        private const val SHADOW_COLOR = 0x11000000
        private const val FPS = 120
    }
}