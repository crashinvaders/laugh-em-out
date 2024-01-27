package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.crashinvaders.common.FleksWorld
import com.github.quillraven.fleks.Entity
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

/**
 * Base class for an action that wraps another action.
 */
abstract class DelegateAction(val action: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action) : com.crashinvaders.laughemout.game.engine.systems.entityactions.Action() {

    protected abstract fun delegate(delta: Float): Boolean

    override fun act(delta: Float): Boolean {
//        Pool pool = getPool();
//        setPool(null); // Ensure this action can't be returned to the pool inside the delegate action.
//        try {
        return delegate(delta)
        //        } finally {
//            setPool(pool);
//        }
    }

    override fun restart() {
        action.restart()
    }

    override fun addedToSystem(world: FleksWorld, entity: Entity) {
        action.addedToSystem(world, entity)
        super.addedToSystem(world, entity)
    }

    override fun removedFromSystem() {
        super.removedFromSystem()
        action.removedFromSystem()
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
}
