package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action
import com.crashinvaders.laughemout.game.engine.systems.entityactions.ParentAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.transform.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/** Returns a new or pooled action of the specified type. */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Action> action(): T {
    val pool = Pools.get(T::class.java)
    val action = pool.obtain()
    action.pool = pool as Pool<Any>
    return action
}
//fun <T : Action> action(type: KClass<out T>): T {
//    val pool = Pools.get(type.java)
//    val action = pool.obtain()
//    action.pool = pool as Pool<Any>
//    return action
//}

@OptIn(ExperimentalContracts::class)
inline fun ParentAction.sequence(
    init: SequenceAction.() -> Unit = {},
) {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    val action = action<SequenceAction>()
    action.init()
    return addAction(action)
}

@OptIn(ExperimentalContracts::class)
inline fun ParentAction.parallel(
    init: ParallelAction.() -> Unit = {},
) {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    val action = action<ParallelAction>()
    action.init()
    return addAction(action)
}

@OptIn(ExperimentalContracts::class)
inline fun ParentAction.repeat(
    repeatTimes: Int = RepeatAction.FOREVER,
    init: RepeatAction.() -> Unit = {},
) {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    val action = action<RepeatAction>()
    action.repeatTimes = repeatTimes
    action.init()
    return addAction(action)
}

fun ParentAction.delay(duration: Float) {
    val action = action<DelayAction>()
    action.duration = duration
    return addAction(action)
}

@OptIn(ExperimentalContracts::class)
inline fun ParentAction.delay(
    duration: Float,
    init: DelayDelegateAction.() -> Unit = {},
) {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    val action = action<DelayDelegateAction>()
    action.duration = duration
    action.init()
    return addAction(action)
}

fun ParentAction.runnable(runnable: (action: RunnableAction) -> Unit) {
    val action = action<RunnableAction>()
    action.runnable = runnable
    return addAction(action)
}

fun ParentAction.interpolate(
    duration: Float,
    interpolation: Interpolation = Interpolation.linear,
    func: (action: InterpolateAction, progress: Float) -> Unit
) {
    val action = action<InterpolateAction>()
    action.duration = duration
    action.interpolation = interpolation
    action.func = func
    return addAction(action)
}

fun ParentAction.moveTo(
    endX: Float,
    endY: Float,
    duration: Float = 0f,
    interpolation: Interpolation = Interpolation.linear,
    space: TransformSpace = TransformSpace.World
) {
    val action = action<MoveToAction>()
    action.endX = endX
    action.endY = endY
    action.duration = duration
    action.interpolation = interpolation
    action.space = space
    return addAction(action)
}

fun ParentAction.moveBy(
    amountX: Float,
    amountY: Float,
    duration: Float = 0f,
    interpolation: Interpolation = Interpolation.linear,
    space: TransformSpace = TransformSpace.World
) {
    val action = action<MoveByAction>()
    action.amountX = amountX
    action.amountY = amountY
    action.duration = duration
    action.interpolation = interpolation
    action.space = space
    return addAction(action)
}

fun ParentAction.scaleTo(
    endX: Float,
    endY: Float,
    duration: Float = 0f,
    interpolation: Interpolation = Interpolation.linear,
    space: TransformSpace = TransformSpace.World
) {
    val action = action<ScaleToAction>()
    action.endX = endX
    action.endY = endY
    action.duration = duration
    action.interpolation = interpolation
    action.space = space
    return addAction(action)
}

fun ParentAction.scaleBy(
    amountX: Float,
    amountY: Float,
    duration: Float = 0f,
    interpolation: Interpolation = Interpolation.linear,
    space: TransformSpace = TransformSpace.World
) {
    val action = action<ScaleByAction>()
    action.amountX = amountX
    action.amountY = amountY
    action.duration = duration
    action.interpolation = interpolation
    action.space = space
    return addAction(action)
}

fun ParentAction.rotateTo(
    end: Float,
    duration: Float = 0f,
    interpolation: Interpolation = Interpolation.linear,
    space: TransformSpace = TransformSpace.World
) {
    val action = action<RotateToAction>()
    action.end = end
    action.duration = duration
    action.interpolation = interpolation
    action.space = space
    return addAction(action)
}

fun ParentAction.rotateBy(
    amount: Float,
    duration: Float = 0f,
    interpolation: Interpolation = Interpolation.linear,
    space: TransformSpace = TransformSpace.World
) {
    val action = action<RotateByAction>()
    action.amount = amount
    action.duration = duration
    action.interpolation = interpolation
    action.space = space
    return addAction(action)
}

fun ParentAction.removeEntity() =
    runnable { action -> action.world -= action.entity }

fun ParentAction.runDelayed(
    duration: Float,
    runnable: (action: RunnableAction) -> Unit) {
    delay(duration) { runnable(runnable) }
}

fun ParentAction.runDelayedInd(
    duration: Float,
    runnable: (action: RunnableAction) -> Unit) {
    delay(duration) {
        (this as Action).timeMode = Action.TimeMode.UnscaledTime
        runnable(runnable)
    }
}