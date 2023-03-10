package com.example.tetrisapp.data.local

import java.util.*
import kotlin.random.Random

class TetrominoRandomizer(
    pieces: Array<String>,
    private val firstPieces: Array<String>,
    private val initialHistory: Array<String>
) : Iterator<String?> {
    private val pool: Array<String>
    private val order = LinkedList<String>()
    private val history = LinkedList<String>()
    private var isFirst = true

    init {
        pool = arrayOf(*pieces, *pieces, *pieces, *pieces, *pieces)
    }

    override fun hasNext(): Boolean {
        return true
    }

    override fun next(): String {
        if (isFirst) {
            isFirst = false
            val firstPiece = firstPieces[Random.nextInt(0, firstPieces.size - 1)]
            history.addAll(listOf(*initialHistory))
            history.add(firstPiece)
            return firstPiece
        }
        lateinit var piece: String
        var ind = 0
        for (roll in 0..5) {
            ind = Random.nextInt(0, pool.size - 1)
            piece = pool[ind]
            if (!history.contains(piece) || roll == 5) {
                break
            }
            if (order.size != 0) pool[ind] = order[0]
        }
        order.remove(piece)
        order.add(piece)
        pool[ind] = order[0]
        history.removeFirst()
        history.add(piece)
        return piece
    }
}