package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

/** Repeats an action a number of times or forever.  */
class RepeatAction() : DelegateAction() {

    var repeatTimes: Int = FOREVER

    private var executeCount = 0
    private var isCompleted = false

    //TODO Replace both of this constructors with
    // constructor(repeatTimes: Int = FOREVER, action: Action) : this() {
    constructor(action: Action) : this(FOREVER, action)
    constructor(repeatTimes: Int, action: Action) : this() {
        this.action = action
        this.repeatTimes = repeatTimes
    }

    override fun delegate(delta: Float): Boolean {
        if (executeCount == repeatTimes) return true
        if (action!!.act(delta)) {
            if (isCompleted) return true
            if (!isAttached) return true // This action was removed.
            if (repeatTimes > 0) executeCount++
            if (executeCount == repeatTimes) return true
            action!!.restart()
        }
        return false
    }

    /** Causes the action to not repeat.  */
    fun finish() {
        isCompleted = true
    }

    override fun restart() {
        executeCount = 0
        isCompleted = false
        super.restart()
    }

    override fun reset() {
        repeatTimes = FOREVER
        super.reset()
    }

    companion object {
        const val FOREVER = -1
    }
}
