package com.crashinvaders.laughemout.game.engine.systems.entityactions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.SnapshotArray
import com.crashinvaders.common.use
import com.github.quillraven.fleks.*
import com.crashinvaders.laughemout.game.engine.components.ActionOwner
import com.crashinvaders.laughemout.game.engine.components.Info
import ktx.assets.pool
import ktx.collections.GdxArrayMap
import ktx.collections.getOrPut
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class EntityActionSystem : IntervalSystem() {

    private val entityActions = GdxArrayMap<Entity, SnapshotArray<Action>>()
    private val actionArrayPool = pool { SnapshotArray<Action>() }

    private lateinit var eGlobalActionRoot: Entity

    private var isProcessingActions = false
    private var hasCompletedActions = false

    override fun onInit() {
        super.onInit()

        eGlobalActionRoot = world.entity {
            it += Info("GlobalActionRoot")
            it += ActionOwner { entity ->  onActionOwnerRemoved(entity) }
        }
    }

    override fun onTick() {
        if (!entityActions.isEmpty) {
            val deltaTime = this.deltaTime

            isProcessingActions = true
            val entitySize = entityActions.size
            for (i in 0 until entitySize) {
                val actions = entityActions.getValueAt(i)
                actions.use { action ->
                    if (!action.isAttached) {
                        hasCompletedActions = true
                        return@use
                    }
                    val isCompleted = action.act(deltaTime)
                    if (isCompleted) {
                        hasCompletedActions = true
                        actions.removeValue(action, true)
                        if (action.isAttached){
                            action.removedFromSystem()
                        }
                    }
                }
            }
            isProcessingActions = false
        }

        if (hasCompletedActions) {
            for (i in (entityActions.size - 1) downTo 0) {
                val actions = entityActions.getValueAt(i)
                if (actions.isEmpty) {
                    val entity = entityActions.getKeyAt(i)
                    entityActions.removeIndex(i)
                    actionArrayPool.free(actions)

                    if (ActionOwner in entity) {
                        entity[ActionOwner].muteRemoveCallback = true
                        entity.configure {
                            it -= ActionOwner
                        }
                    }
                }
            }
        }
    }

    /** Run an action without it's being bound to an entity. */
    fun addAction(action: Action) =
        addAction(eGlobalActionRoot, action)

    fun addAction(entity: Entity, action: Action) {
        if (isProcessingActions) {
            Gdx.app.postRunnable { addAction(entity, action) }
            return
        }

        if (ActionOwner !in entity) {
            entity.configure {
                it += ActionOwner { entity -> onActionOwnerRemoved(entity) }
            }
        }

        val actions = entityActions.getOrPut(entity) { actionArrayPool.obtain() }
        actions.add(action)
        action.addedToSystem(world, entity)
    }

    fun removeActions(entity: Entity) {
        if (isProcessingActions) {
            Gdx.app.postRunnable { removeActions(entity) }
            return
        }

        entity.configure {
            entity -= ActionOwner
        }
    }

    private fun onActionOwnerRemoved(entity: Entity) {
        val actions = entityActions[entity]
        if (actions == null) {
            return
        }

        hasCompletedActions = true
        actions.forEach { it.removedFromSystem() }
        actions.clear()
    }

    @OptIn(ExperimentalContracts::class)
    inline fun actions(
        entity: Entity? = null,
        init: ParentAction.() -> Unit,
    ) {
        contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
        val parentAction: ParentAction = if (entity != null) {
            ParentAction { action -> this@EntityActionSystem.addAction(entity, action) }
        } else {
            ParentAction { action -> this@EntityActionSystem.addAction(action) }
        }
        parentAction.init()
    }
}
