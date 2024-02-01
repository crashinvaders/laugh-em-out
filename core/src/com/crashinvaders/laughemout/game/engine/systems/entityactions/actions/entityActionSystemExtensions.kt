package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.github.quillraven.fleks.Entity

fun EntityActionSystem.schedule(
    delay: Float,
    timeMode: Action.TimeMode = Action.TimeMode.GameTime,
    runnable: (action: Action) -> Unit
) {
    schedule(eGlobalActionRoot, delay, timeMode, runnable)
}

fun EntityActionSystem.schedule(
    entity: Entity = eGlobalActionRoot,
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
    scheduleInd(eGlobalActionRoot, delay, runnable)
}

fun EntityActionSystem.scheduleInd(
    entity: Entity,
    delay: Float,
    runnable: (action: Action) -> Unit
) {
    schedule(entity, delay, Action.TimeMode.UnscaledTime, runnable)
}