package com.example.tetrisapp.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.tetrisapp.R
import com.example.tetrisapp.model.game.Piece

class PieceView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()
    private var piece: Piece? = null
    private var pointSize = 0
    private var xOffset = 0
    private var yOffset = 0
    private var color = -0x1000000

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PieceView,
            0, 0
        )
        color = try {
            a.getColor(R.styleable.PieceView_backgroundColor, color)
        } finally {
            a.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateDimensions(w, h)
    }

    private fun calculateDimensions(width: Int, height: Int) {
        if (piece != null) {
            var pieceHeight = -1
            var pieceWidth = -1
            for (y in piece!!.matrix.indices) {
                for (x in piece!!.matrix[y].indices) {
                    if (piece!!.matrix[y][x].toInt() == 1) {
                        pieceHeight = Math.max(pieceHeight, y + 1)
                        pieceWidth = Math.max(pieceWidth, x + 1)
                    }
                }
            }
            pointSize = Math.min(height / pieceHeight, width / pieceWidth)
            xOffset = (width - pointSize * pieceWidth) / 2
            yOffset = (height - pointSize * pieceHeight) / 2
        }
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(color)
        if (piece != null) {
            drawTetromino(canvas)
        }
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
        val overlay = VectorDrawableCompat.create(resources, overlayResource, null)!!
        overlay.mutate()
        overlay.setBounds(0, 0, pointSize, pointSize)
        canvas.translate((x * pointSize + xOffset).toFloat(), (y * pointSize + yOffset).toFloat())
        overlay.draw(canvas)
        canvas.translate(-(x * pointSize + xOffset).toFloat(), -(y * pointSize + yOffset).toFloat())
    }

    private fun drawTetromino(canvas: Canvas) {
        for (y in piece!!.matrix.indices) {
            if (y >= piece!!.matrix.size) continue
            for (x in piece!!.matrix[y].indices) {
                if (x < piece!!.matrix[y].size && piece!!.matrix[y][x].toInt() == 1) {
                    drawPoint(x, y, piece!!.color, piece!!.overlayResId, canvas)
                }
            }
        }
    }

    fun setPiece(piece: Piece?) {
        this.piece = piece
        calculateDimensions(width, height)
        invalidate()
    }
}