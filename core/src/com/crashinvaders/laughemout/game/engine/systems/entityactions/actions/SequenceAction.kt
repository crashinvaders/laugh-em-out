package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

class SequenceAction() : ParallelAction() {

    private var index = 0

    constructor(action1: Action) : this() {
        addAction(action1)
    }

    constructor(action1: Action, action2: Action) : this() {
        addAction(action1)
        addAction(action2)
    }

    constructor(action1: Action, action2: Action, action3: Action) : this() {
        addAction(action1)
        addAction(action2)
        addAction(action3)
    }

    constructor(action1: Action, action2: Action, action3: Action, action4: Action) : this() {
        addAction(action1)
        addAction(action2)
        addAction(action3)
        addAction(action4)
    }

    constructor(action1: Action, action2: Action, action3: Action, action4: Action, action5: Action) : this() {
        addAction(action1)
        addAction(action2)
        addAction(action3)
        addAction(action4)
        addAction(action5)
    }

    override fun act(delta: Float): Boolean {
        if (index >= actions.size) return true
//        val assignedPool = this.pool
//        this.pool = null // Ensure this action can't be returned to the pool inside the delegate action.
//        try {
            if (actions[index].act(delta)) {
                if (!isAttached) return true // This action was removed.
                index++
                if (index >= actions.size) return true
            }
            return false
//        } finally {
//            this.pool = assignedPool
//        }
    }

    override fun restart() {
        super.restart()
        index = 0
    }
}
