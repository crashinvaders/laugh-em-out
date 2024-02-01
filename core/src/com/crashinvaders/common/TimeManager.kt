package com.crashinvaders.common

import ktx.collections.GdxArray

class TimeManager {

    private val tokens = GdxArray<Token>()
    private var tokenStartIndex = 0
    private var isTokenDirty = false

    var delta: Float = 0f; private set
    var unscaledDelta: Float = 0f; private set

    var time: Float = 0f; private set
    var unscaledTime: Float = 0f; private set

    fun addToken(token: Token) {
        tokens.add(token)
        isTokenDirty = true
    }

    fun removeToken(token: Token) {
        if (tokens.removeValue(token, true)) {
            isTokenDirty = true
        }
    }

    fun process(unscaledDelta: Float): Float {
        if (isTokenDirty) {
            isTokenDirty = false

            tokenStartIndex = tokens.indexOfLast { it.overrideState }
            if (tokenStartIndex < 0) {
                tokenStartIndex = 0
            }
        }

        var timeFactor = 1f
        if (tokens.size > 0) {
            for (i in tokenStartIndex until tokens.size) {
                timeFactor *= tokens[i].timeFactor
            }
        }

        this.unscaledDelta = unscaledDelta;
        this.delta = unscaledDelta * timeFactor

        this.unscaledTime += unscaledDelta
        this.time += delta

        return delta
    }

    data class Token(
        val name: String,
        val order: Int,
        val overrideState: Boolean,
        var timeFactor: Float = 1f)
}