package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

/** Repeats an action a number of times or forever.  */
class RepeatAction(
    action: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action,
    val repeatTimes: Int = FOREVER
) : DelegateAction(action) {

    private var executeCount = 0

    private var isCompleted = false

    override fun delegate(delta: Float): Boolean {
        if (executeCount == repeatTimes) return true
        if (action.act(delta)) {
            if (isCompleted) return true
            if (repeatTimes > 0) executeCount++
            if (executeCount == repeatTimes) return true
            action.restart()
        }
        return false
    }

    /** Causes the action to not repeat.  */
    fun finish() {
        isCompleted = true
    }

    override fun restart() {
        super.restart()
        executeCount = 0
        isCompleted = false
    }

    companion object {
        const val FOREVER = -1
    }
}
