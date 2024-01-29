package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

typealias Runnable = ((action: RunnableAction) -> Unit)

/** An action that runs a [Runnable]. Alternatively, the [.run] method can be overridden instead of setting a runnable.  */
class RunnableAction() : Action() {

    var runnable: Runnable? = null

    private var ran = false

    constructor(runnable: Runnable) : this() {
        this.runnable = runnable
    }

    override fun act(delta: Float): Boolean {
        if (!ran) {
            ran = true
            run()
        }
        return true
    }

    /** Called to run the runnable.  */
    private fun run() {
        val assignedPool = this.pool
        this.pool = null // Ensure this action can't be returned to the pool inside the delegate action.
        try {
            runnable!!(this)
        } finally {
            this.pool = assignedPool
        }
    }

    override fun restart() {
        ran = false
    }

    override fun reset() {
        super.reset()
        runnable = null
    }
}
