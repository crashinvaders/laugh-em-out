package com.crashinvaders.laughemout.game.common

import com.badlogic.gdx.math.MathUtils
import com.crashinvaders.laughemout.game.engine.components.SodInterpolation

object SodUtils {

    fun SodInterpolation.kickVisually(factor: Float = 1f, rotate: Boolean = true, scale: Boolean = true) {
        this.setAccel(0f, 0f,
            if (rotate) MathUtils.random(-90f, +90f) * factor else 0f,
            if (scale) +50f * factor else 0f,
            if (scale) -20f * factor else 0f)
    }
}