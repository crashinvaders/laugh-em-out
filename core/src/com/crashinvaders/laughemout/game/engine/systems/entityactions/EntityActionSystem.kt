package com.crashinvaders.laughemout.game.engine.systems.entityactions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.SnapshotArray
import com.crashinvaders.common.use
import com.github.quillraven.fleks.*
import com.crashinvaders.laughemout.game.engine.components.ActionOwner
import ktx.collections.GdxArrayMap
import ktx.collections.getOrPut

class EntityActionSystem : IntervalSystem() {

    private val activeActions = GdxArrayMap<Entity, SnapshotArray<com.crashinvaders.laughemout.game.engine.systems.entityactions.Action>>()

    private var isUpdating = false

    override fun onTick() {
        val deltaTime = this.deltaTime

        var hasCompletedActions = false

        isUpdating = true
        val entitySize = activeActions.size
        for (i in 0 until entitySize) {
            val actions = activeActions.getValueAt(i)
            actions.use { action ->
                val isCompleted = action.act(deltaTime)
                if (isCompleted) {
                    hasCompletedActions = true
                    actions.removeValue(action, true)
                    action.removedFromSystem()
                }
            }
        }
        isUpdating = false

        if (hasCompletedActions) {
            for (i in (activeActions.size - 1) downTo 0) {
                //TODO Pool snapshot array.
                val actions = activeActions.getValueAt(i)
                if (actions.isEmpty) {
                    val entity = activeActions.getKeyAt(i)
                    activeActions.removeIndex(i)
                    //TODO Mute component's removal callback.
                    entity.configure {
                        it -= ActionOwner
                    }
                }
            }
        }
    }

    /** Run an action without it's being bound to an entity. */
    fun addAction(action: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action) =
        addAction(Entity.NONE, action)

    fun addAction(entity: Entity, action: com.crashinvaders.laughemout.game.engine.systems.entityactions.Action) {
        if (isUpdating) {
            Gdx.app.postRunnable { addAction(entity, action) }
            return
        }

        if (ActionOwner !in entity) {
            entity.configure {
                it += ActionOwner { entity -> onActionOwnerRemoved(entity) }
            }
        }

        //TODO Pool snapshot array.
        val actions = activeActions.getOrPut(entity) { SnapshotArray() }
        actions.add(action)
        action.addedToSystem(world, entity)
    }

    fun removeActions(entity: Entity) {
        if (isUpdating) {
            Gdx.app.postRunnable { removeActions(entity) }
            return
        }

        entity.configure {
            entity -= ActionOwner
        }
    }

    private fun onActionOwnerRemoved(entity: Entity) {
        val actions = activeActions[entity]
        if (actions == null) {
            return
        }

//        debug { "Entity ${entity.getPrintName(world)} is removing and still has ${actions.size} actions running." }
        activeActions.removeKey(entity)
        actions.forEach { it.removedFromSystem() }
    }
}
