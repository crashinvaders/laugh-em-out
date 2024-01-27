package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.badlogic.gdx.utils.Array
import com.crashinvaders.common.FleksWorld
import com.github.quillraven.fleks.Entity
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action
import ktx.collections.gdxArrayOf

open class ParallelAction : com.crashinvaders.laughemout.game.engine.systems.entityactions.Action {

    val actions: Array<com.crashinvaders.laughemout.game.engine.systems.entityactions.Action> = gdxArrayOf(initialCapacity = 4)

    private var isCompleted = false

    constructor()

    constructor(action1: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action) {
        addAction(action1)
    }

    constructor(action1: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action2: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action) {
        addAction(action1)
        addAction(action2)
    }

    constructor(action1: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action2: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action3: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action) {
        addAction(action1)
        addAction(action2)
        addAction(action3)
    }

    constructor(action1: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action2: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action3: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action4: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action) {
        addAction(action1)
        addAction(action2)
        addAction(action3)
        addAction(action4)
    }

    constructor(action1: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action2: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action3: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action4: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action, action5: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action) {
        addAction(action1)
        addAction(action2)
        addAction(action3)
        addAction(action4)
        addAction(action5)
    }

    override fun act(delta: Float): Boolean {
        if (isCompleted) return true
        isCompleted = true
        //        Pool pool = getPool();
//        setPool(null); // Ensure this action can't be returned to the pool while executing.
//        try {
        val actions: Array<com.crashinvaders.laughemout.game.engine.systems.entityactions.Action> = actions
        var i = 0
        val n = actions.size
        while (i < n && isAttached) {
            val currentAction: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action = actions[i]
            if (currentAction.isAttached && !currentAction.act(delta)) isCompleted = false
            if (!isAttached) return true // This action was removed.
            i++
        }
        return isCompleted
        //        } finally {
//            setPool(pool);
//        }
    }

    override fun restart() {
        isCompleted = false
        val actions: Array<com.crashinvaders.laughemout.game.engine.systems.entityactions.Action> = actions
        var i = 0
        val n = actions.size
        while (i < n) {
            actions[i].restart()
            i++
        }
    }

    override fun reset() {
        super.reset()
        actions.clear()
    }

    fun addAction(action: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action) {
        actions.add(action)
        if (isAttached) action.addedToSystem(world, entity)
    }

    override fun addedToSystem(world: FleksWorld, entity: Entity) {
        val actions: Array<com.crashinvaders.laughemout.game.engine.systems.entityactions.Action> = actions
        var i = 0
        val n = actions.size
        while (i < n) {
            actions[i].addedToSystem(world, entity)
            i++
        }
        super.addedToSystem(world, entity)
    }

    override fun removedFromSystem() {
        val actions: Array<com.crashinvaders.laughemout.game.engine.systems.entityactions.Action> = actions
        var i = 0
        val n = actions.size
        while (i < n) {
            actions[i].removedFromSystem()
            i++
        }
        super.removedFromSystem()
    }

    //    @Override
    //    public void setEntity (Entity entity) {
    //        Array<Action> actions = this.actions;
    //        for (int i = 0, n = actions.size; i < n; i++)
    //            actions.get(i).setEntity(entity);
    //        super.setEntity(entity);
    //    }

    override fun toString(): String {
        val buffer = StringBuilder(64)
        buffer.append(super.toString())
        buffer.append('(')
        val actions: Array<com.crashinvaders.laughemout.game.engine.systems.entityactions.Action> = actions
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