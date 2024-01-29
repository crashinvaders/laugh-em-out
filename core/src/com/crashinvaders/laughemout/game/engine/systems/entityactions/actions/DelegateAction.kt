package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.crashinvaders.common.FleksWorld
import com.github.quillraven.fleks.Entity
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action
import com.crashinvaders.laughemout.game.engine.systems.entityactions.ParentAction
import ktx.app.gdxError

/**
 * Base class for an action that wraps another action.
 */
abstract class DelegateAction() : Action(), ParentAction {

    protected var action: Action? = null

    protected abstract fun delegate(delta: Float): Boolean

    constructor(action: Action) : this() {
        this.action = action
    }

    override fun act(delta: Float): Boolean {
        val assignedPool = this.pool
        this.pool = null // Ensure this action can't be returned to the pool inside the delegate action.
        try {
            return delegate(delta)
        } finally {
            this.pool = assignedPool
        }
    }

    override fun addedToSystem(world: FleksWorld, entity: Entity) {
        action!!.addedToSystem(world, entity)
        super.addedToSystem(world, entity)
    }

    override fun removedFromSystem() {
        action!!.removedFromSystem()
        super.removedFromSystem()
    }

    override fun restart() {
        action?.restart()
        super.restart()
    }

    override fun reset() {
        action = null
        super.reset()
    }
//    @Override
    //    public void setEntity(Entity entity) {
    //        if (action != null) action.setEntity(entity);
    //        super.setEntity(entity);
    //    }
    //    public void setTarget (Actor target) {
    //        if (action != null) action.setTarget(target);
    //        super.setTarget(target);
    //    }

    override fun toString(): String {
        return super.toString() + if (action == null) "" else "($action)"
    }

    override fun addAction(action: Action) {
        if (this.action != null) {
            gdxError("DelegateAction can have only one child.")
        }
        this.action = action
    }
}
