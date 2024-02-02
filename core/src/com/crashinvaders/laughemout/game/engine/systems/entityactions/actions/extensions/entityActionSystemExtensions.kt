package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.extensions

import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.DelayDelegateAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.RunnableAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.action
import com.github.quillraven.fleks.Entity

fun EntityActionSystem.schedule(
    delay: Float,
    timeMode: Action.TimeMode = Action.TimeMode.GameTime,
    runnable: (action: Action) -> Unit
) {
    schedule(globalActionHost, delay, timeMode, runnable)
}

fun EntityActionSystem.schedule(
    entity: Entity = globalActionHost,
    delay: Float,
    timeMode: Action.TimeMode = Action.TimeMode.GameTime,
    runnable: (action: Action) -> Unit
) {
    val runnableAction = action<RunnableAction>()
    runnableAction.runnable = runnable

    val delayAction = action<DelayDelegateAction>()
    delayAction.duration = delay
    delayAction.timeMode = timeMode
    delayAction.addAction(runnableAction)

    addAction(entity, delayAction)
}

fun EntityActionSystem.scheduleInd(
    delay: Float,
    runnable: (action: Action) -> Unit
) {
    scheduleInd(globalActionHost, delay, runnable)
}

fun EntityActionSystem.scheduleInd(
    entity: Entity,
    delay: Float,
    runnable: (action: Action) -> Unit
) {
    schedule(entity, delay, Action.TimeMode.UnscaledTime, runnable)
}