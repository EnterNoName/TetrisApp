package com.michaelrayven.tetris_jetpack.core.util

fun <T>Array<Array<T>>.deepCopy(): Array<Array<T>> {
    val arrayCopy = this.copyOf()
    for (i in arrayCopy.indices) {
        val new = arrayCopy[i].copyOf()
        arrayCopy[i] = new
    }
    return arrayCopy
}