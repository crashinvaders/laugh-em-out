package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.extensions

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.ParentAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.DelegateAction
import com.github.quillraven.fleks.Entity
import ktx.ai.GdxAiDsl
import ktx.app.gdxError

//@OptIn(ExperimentalContracts::class)
@GdxAiDsl
fun <T> Task<T>.entityAction(
    world: FleksWorld,
    entity: Entity? = null,
    actionProducer: ParentAction.(T) -> Unit
): Int {
//    contract { callsInPlace(actionProducer, InvocationKind.EXACTLY_ONCE) }
    val task = ActionBasedTask(world, entity, actionProducer)
    return addChild(task)
}

class ActionBasedTask<T>(
    world: FleksWorld,
    private val entity: Entity? = null,
    private val actionProducer: ParentAction.(T) -> Unit,
) : LeafTask<T>() {

    private val actionSystem = world.system<EntityActionSystem>()

    private val wrapperAction = WrapperAction {
        isActionCompleted = true
    }

    private var isActionCompleted = false

    override fun start() {
        super.start()

        wrapperAction.actionProducer(`object`)

        if (entity != null) {
            actionSystem.addAction(entity, wrapperAction)
        } else {
            actionSystem.addAction(wrapperAction)
        }
    }

    override fun end() {
        super.end()

        actionSystem.removeAction(wrapperAction)
    }

    override fun resetTask() {
        isActionCompleted = false
        wrapperAction.reset()
        super.resetTask()
    }

    override fun execute(): Status =
        if (isActionCompleted) Status.SUCCEEDED else Status.RUNNING

    override fun copyTo(task: Task<T>?): Task<T> {
        TODO("Not yet implemented")
    }

    private class WrapperAction(
        val onRemoved: () -> Unit
    ) : Action(), ParentAction {

        private var action: Action? = null

        override fun act(delta: Float): Boolean {
            if (action != null) {
                return action!!.act(delta)
            }
            return true
        }

        override fun addedToSystem(world: FleksWorld, entity: Entity) {
            action?.addedToSystem(world, entity)
            super.addedToSystem(world, entity)
        }

        override fun removedFromSystem() {
            action?.removedFromSystem()
            super.removedFromSystem()

            onRemoved.invoke()
        }

        override fun restart() {
            action?.restart()
            super.restart()
        }

        override fun reset() {
            action = null
            super.reset()
        }

        override fun toString(): String {
            return super.toString() + if (action == null) "" else "($action)"
        }

        override fun addAction(action: Action) {
            if (this.action != null) {
                gdxError("ActionBasedTask can host only one action.")
            }
            this.action = action
        }
    }
}