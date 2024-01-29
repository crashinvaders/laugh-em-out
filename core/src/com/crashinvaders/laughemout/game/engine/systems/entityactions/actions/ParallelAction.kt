package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.badlogic.gdx.utils.Array
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action
import com.crashinvaders.laughemout.game.engine.systems.entityactions.ParentAction
import com.github.quillraven.fleks.Entity
import ktx.collections.gdxArrayOf

open class ParallelAction() : Action(), ParentAction {

    val actions: Array<Action> = gdxArrayOf(initialCapacity = 4)

    private var isCompleted = false

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
        if (isCompleted) {
            return true
        }

        isCompleted = true

//        val assignedPool = this.pool
//        this.pool = null // Ensure this action can't be returned to the pool inside the delegate action.
//        try {
            val actions: Array<Action> = actions
            var i = 0
            val n = actions.size
            while (i < n && isAttached) {
                val currentAction: Action = actions[i]
                if (currentAction.isAttached && !currentAction.act(delta)) {
                    isCompleted = false
                }
                if (!isAttached) {
                    return true // This action was removed.
                }
                i++
            }
            return isCompleted
//        } finally {
//            this.pool = assignedPool
//        }
    }

    override fun addAction(action: Action) {
        actions.add(action)
        if (isAttached) action.addedToSystem(world!!, entity!!)
    }

    override fun addedToSystem(world: FleksWorld, entity: Entity) {
        val actions: Array<Action> = actions
        var i = 0
        val n = actions.size
        while (i < n) {
            actions[i].addedToSystem(world, entity)
            i++
        }
        super.addedToSystem(world, entity)
    }

    override fun removedFromSystem() {
        val actions: Array<Action> = actions
        var i = 0
        val n = actions.size
        while (i < n) {
            actions[i].removedFromSystem()
            i++
        }
        super.removedFromSystem()
    }

    override fun restart() {
        isCompleted = false
        val actions: Array<Action> = actions
        var i = 0
        val n = actions.size
        while (i < n) {
            actions[i].restart()
            i++
        }
        super.restart()
    }

    override fun reset() {
        super.reset()
        actions.clear()
    }

    override fun toString(): String {
        val buffer = StringBuilder(64)
        buffer.append(super.toString())
        buffer.append('(')
        val actions: Array<Action> = actions
        var i = 0
        val n = actions.size
        while (i < n) {
            if (i > 0) buffer.append(", ")
            buffer.append(actions[i])
            i++
        }
        buffer.append(')')
        return buffer.toString()
    }
}
