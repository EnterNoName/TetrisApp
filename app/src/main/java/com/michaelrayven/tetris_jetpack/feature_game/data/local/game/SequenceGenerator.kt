package com.michaelrayven.tetris_jetpack.feature_game.data.local.game

import java.util.LinkedList
import kotlin.random.Random

class SequenceGenerator(
    val tetrominoes: Array<String>,
    val possibleFirstTetrominoes: Array<String>,
    val initialHistory: Array<String>
): Iterator<String> {
    private val pool: Array<String> = arrayOf(*tetrominoes, *tetrominoes, *tetrominoes, *tetrominoes, *tetrominoes)
    private val order = LinkedList<String>()
    private val history = LinkedList<String>()
    private var isFirst = true

    override fun hasNext(): Boolean {
        return true
    }

    override fun next(): String {
        if (isFirst) {
            isFirst = false
            val firstPiece = possibleFirstTetrominoes[Random.nextInt(0, possibleFirstTetrominoes.size - 1)]
            history.addAll(initialHistory)
            history.add(firstPiece)
            return firstPiece
        }

        lateinit var tetromino: String
        var i = 0
        for (roll in 0..5) {
            i = Random.nextInt(0, pool.size - 1)
            tetromino = pool[i]
            if (!history.contains(tetromino) || roll == 5) {
                break
            }
            if (order.size != 0) pool[i] = order[0]
        }
        order.remove(tetromino)
        order.add(tetromino)
        pool[i] = order[0]
        history.removeFirst()
        history.add(tetromino)
        return tetromino
    }

}