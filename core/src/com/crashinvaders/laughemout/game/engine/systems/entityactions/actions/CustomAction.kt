package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

class CustomAction(
    val apply: (Float) -> Unit,
    val duration: Float,
    val interpolation: Interpolation = Interpolation.linear
) : com.crashinvaders.laughemout.game.engine.systems.entityactions.Action() {

    private var timePassed = 0f

    override fun restart() {
        timePassed = 0f
        super.restart()
    }

    override fun act(delta: Float): Boolean {
        if (timePassed >= duration) {
            return true
        }

        timePassed += delta
        val rawT = MathUtils.clamp(timePassed / duration, 0f, 1f)
        val interpolatedT = interpolation.apply(rawT)
        apply(interpolatedT)

        return timePassed >= duration
    }
}
