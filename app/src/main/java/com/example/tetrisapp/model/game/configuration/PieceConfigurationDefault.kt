package com.example.tetrisapp.model.game.configuration

import com.example.tetrisapp.R
import com.example.tetrisapp.interfaces.PieceConfiguration
import com.example.tetrisapp.model.game.Piece

class PieceConfigurationDefault : PieceConfiguration {
    var pieceMap: MutableMap<String, Piece> = HashMap()

    init {
        val pieces = arrayOf(
                Piece(
                    "I",
                    arrayOf(
                        byteArrayOf(0, 0, 0, 0),
                        byteArrayOf(1, 1, 1, 1),
                        byteArrayOf(0, 0, 0, 0),
                        byteArrayOf(0, 0, 0, 0)
                    ),
                    -0xff0001,
                    R.drawable.point_overlay_default
                ),
                Piece(
                    "J", arrayOf(byteArrayOf(1, 0, 0), byteArrayOf(1, 1, 1), byteArrayOf(0, 0, 0)),
                    -0xffff01,
                    R.drawable.point_overlay_default
                ),
                Piece(
                    "L", arrayOf(byteArrayOf(0, 0, 1), byteArrayOf(1, 1, 1), byteArrayOf(0, 0, 0)),
                    -0x8100,
                    R.drawable.point_overlay_default
                ),
                Piece(
                    "O", arrayOf(byteArrayOf(1, 1), byteArrayOf(1, 1)),
                    -0x100,
                    R.drawable.point_overlay_default
                ),
                Piece(
                    "S", arrayOf(byteArrayOf(0, 1, 1), byteArrayOf(1, 1, 0), byteArrayOf(0, 0, 0)),
                    -0xff0100,
                    R.drawable.point_overlay_default
                ),
                Piece(
                    "Z", arrayOf(byteArrayOf(1, 1, 0), byteArrayOf(0, 1, 1), byteArrayOf(0, 0, 0)),
                    -0x10000,
                    R.drawable.point_overlay_default
                ),
                Piece(
                    "T", arrayOf(byteArrayOf(0, 1, 0), byteArrayOf(1, 1, 1), byteArrayOf(0, 0, 0)),
                    -0x7fff80,
                    R.drawable.point_overlay_default
                )
            )

        pieces.forEach {
            pieceMap[it.name] = it
        }
    }

    override val pieces: Array<Piece>
        get() = pieceMap.values.toTypedArray()
    override val names: Array<String>
        get() = pieceMap.keys.toTypedArray()

    override operator fun get(name: String): Piece {
        return pieceMap.getOrDefault(name, Piece(
            "XXX", arrayOf(
                byteArrayOf(1)
            ),
            -0xcecece,
            R.drawable.point_overlay_default
        ))
    }

    override val starterPieces: Array<String>
        get() = arrayOf("I", "J", "L", "T")
    override val initialHistory: Array<String>
        get() = arrayOf("Z", "S", "Z", "S")
}