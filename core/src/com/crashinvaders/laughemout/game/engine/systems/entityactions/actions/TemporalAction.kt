package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.badlogic.gdx.math.Interpolation
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

/**
 * Base class for actions that transition over time using the percent complete.
 */
abstract class TemporalAction() : Action() {

    var duration: Float = 0f
    var interpolation: Interpolation = Interpolation.linear

    var time = 0f
    /** When true, the action's progress will go from 100% to 0%.  */
    var isReverse = false

    private var began = false
    private var complete = false

    constructor(duration: Float = 0f, interpolation: Interpolation = Interpolation.linear) : this() {
        this.duration = duration
        this.interpolation = interpolation
    }

    override fun act(delta: Float): Boolean {
        if (complete) {
            return true
        }

        val assignedPool = this.pool
        this.pool = null // Ensure this action can't be returned to the pool inside the delegate action.
        try {
            if (!began) {
                begin()
                began = true
            }
            time += delta
            complete = time >= duration
            var percent: Float
            if (complete) percent = 1f else {
                percent = time / duration
                percent = interpolation.apply(percent)
            }
            update(if (isReverse) 1 - percent else percent)
            if (complete) end()
            return complete
        } finally {
            this.pool = assignedPool
        }
    }

    /** Called the first time [.act] is called. This is a good place to query the [actor&#39;s][.actor] starting
     * state.  */
    protected open fun begin() {}

    /** Called the last time [.act] is called.  */
    protected open fun end() {}

    /** Called each frame.
     * @param percent The percentage of completion for this action, growing from 0 to 1 over the duration. If
     * [reversed][.setReverse], this will shrink from 1 to 0.
     */
    protected abstract fun update(percent: Float)

    /** Skips to the end of the transition.  */
    fun finish() {
        time = duration
    }

    override fun restart() {
        time = 0f
        began = false
        complete = false
        super.restart()
    }

    override fun reset() {
        isReverse = false
        duration = 0f
        interpolation = Interpolation.linear
        super.reset()
    }
}
