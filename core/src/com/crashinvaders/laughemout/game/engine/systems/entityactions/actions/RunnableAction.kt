package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

/** An action that runs a [Runnable]. Alternatively, the [.run] method can be overridden instead of setting a runnable.  */
class RunnableAction(
    val runnable: () -> Unit
) : com.crashinvaders.laughemout.game.engine.systems.entityactions.Action() {

    private var ran = false

    override fun act(delta: Float): Boolean {
        if (!ran) {
            ran = true
            run()
        }
        return true
    }

    /** Called to run the runnable.  */
    private fun run() {
        runnable()
//        val pool: Pool<*> = getPool()
//        setPool(null) // Ensure this action can't be returned to the pool inside the runnable.
//        try {
//            if (runnable != null) {
//                runnable!!.run()
//            }
//        } finally {
//            setPool(pool)
//        }
    }

    override fun restart() {
        ran = false
    }
}
