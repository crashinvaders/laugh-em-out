package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action
import com.github.quillraven.fleks.Entity

typealias Func = ((action: InterpolateAction, progress: Float) -> Unit)

class InterpolateAction() : Action() {

    var duration: Float = 0f
    var interpolation: Interpolation = Interpolation.linear
    var func: Func? = null

    private var timePassed = 0f

    constructor(
        duration: Float,
        interpolation: Interpolation = Interpolation.linear,
        func: Func,
    ) : this() {
        this.duration = duration
        this.interpolation = interpolation
        this.func = func
    }

    override fun act(delta: Float): Boolean {
        if (timePassed >= duration) {
            return true
        }

        timePassed += delta
        val rawT = MathUtils.clamp(timePassed / duration, 0f, 1f)
        val interpolatedT = interpolation.apply(rawT)
        func!!(this, interpolatedT)

        return timePassed >= duration
    }

    override fun restart() {
        timePassed = 0f
        super.restart()
    }

    override fun reset() {
        duration = 0f
        interpolation = Interpolation.linear
        func = null
        super.reset()
    }
}
