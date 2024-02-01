package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

/** Executes an action after delay. */
class DelayDelegateAction() : DelegateAction() {

    var duration: Float = 0f

    private var time = 0f

    constructor(duration: Float, action: Action) : this() {
        this.duration = duration
        this.action = action
    }

    override fun delegate(delta: Float): Boolean {
        var deltaLocal = delta
        if (time < duration) {
            time += deltaLocal
            if (time < duration) {
                return false
            }
            deltaLocal = delta - (time - duration)
        }
        return action!!.act(deltaLocal)
    }

    override fun restart() {
        time = 0f
        super.restart()
    }

    override fun reset() {
        duration = 0f
        super.reset()
    }
}
