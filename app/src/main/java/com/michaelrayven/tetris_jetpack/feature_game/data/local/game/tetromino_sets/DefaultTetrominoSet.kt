package com.michaelrayven.tetris_jetpack.feature_game.data.local.game.tetromino_sets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.michaelrayven.tetris_jetpack.R
import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.Tetromino

class DefaultTetrominoSet : TetrominoSet {
    override val tetrominoes: Array<Tetromino> = arrayOf(
        Tetromino(
            name = "I",
            shapeMatrix = arrayOf(
                arrayOf(false, true, false, false),
                arrayOf(false, true, false, false),
                arrayOf(false, true, false, false),
                arrayOf(false, true, false, false)
            ),
            color = -0xff0001,
            overlayResId = R.drawable.point_overlay_default
        ),
        Tetromino(
            name = "J",
            shapeMatrix = arrayOf(
                arrayOf(false, true, false),
                arrayOf(false, true, false),
                arrayOf(true, true, false)
            ),
            color = -0xffff01,
            overlayResId = R.drawable.point_overlay_default
        ),
        Tetromino(
            name = "L",
            shapeMatrix = arrayOf(
                arrayOf(true, true, false),
                arrayOf(false, true, false),
                arrayOf(false, true, false)
            ),
            color = -0x8100,
            overlayResId = R.drawable.point_overlay_default
        ),
        Tetromino(
            name = "O",
            shapeMatrix = arrayOf(
                arrayOf(true, true),
                arrayOf(true, true)
            ),
            color = -0x100,
            overlayResId = R.drawable.point_overlay_default
        ),
        Tetromino(
            name = "S",
            shapeMatrix = arrayOf(
                arrayOf(true, false, false),
                arrayOf(true, true, false),
                arrayOf(false, true, false)
            ),
            color = -0xff0100,
            overlayResId = R.drawable.point_overlay_default
        ),
        Tetromino(
            name = "Z",
            shapeMatrix = arrayOf(
                arrayOf(false, true, false),
                arrayOf(true, true, false),
                arrayOf(true, false, false)
            ),
            color = -0x10000,
            overlayResId = R.drawable.point_overlay_default
        ),
        Tetromino(
            name = "T",
            shapeMatrix = arrayOf(
                arrayOf(false, true, false),
                arrayOf(true, true, false),
                arrayOf(false, true, false)
            ),
            color = -0x7fff80,
            overlayResId = R.drawable.point_overlay_default
        )
    )
    override val names: Array<String> = tetrominoes.map { it.name }.toTypedArray()
    override val possibleFirstTetrominoes: Array<String> = arrayOf("I", "J", "L", "T")
    override val initialHistory: Array<String> = arrayOf("Z", "S", "Z", "S")

    override val shadowColor: Int = Color.LightGray.toArgb()
    override val shadowName: String = "SHADOW"

    override fun get(name: String): Tetromino? {
        return tetrominoes.find { it.name == name }
    }
}