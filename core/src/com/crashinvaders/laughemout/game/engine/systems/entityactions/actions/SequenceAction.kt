package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

class SequenceAction : com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.ParallelAction {

    private var index = 0

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

    override fun addedToSystem(world: World, entity: Entity) {
        super.addedToSystem(world, entity)
    }

    override fun removedFromSystem() {
        super.removedFromSystem()
    }

    override fun act(delta: Float): Boolean {
        if (index >= actions.size) return true
        //        Pool pool = getPool();
//        setPool(null); // Ensure this action can't be returned to the pool while executings.
//        try {
        if (actions[index].act(delta)) {
            if (!isAttached) return true // This action was removed.
            index++
            if (index >= actions.size) return true
        }
        return false
        //        } finally {
//            setPool(pool);
//        }
    }

    override fun restart() {
        super.restart()
        index = 0
    }
}
