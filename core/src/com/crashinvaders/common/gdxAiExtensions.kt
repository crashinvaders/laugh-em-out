@file:JvmName("GdxAiExtensions")

package com.crashinvaders.common

import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.ai.btree.Decorator
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.branch.Parallel
import com.badlogic.gdx.utils.Array
import ktx.ai.GdxAiDsl
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
@GdxAiDsl
fun <E> Task<E>.runnable(
    runnable: (E) -> Unit
): Int {
    contract { callsInPlace(runnable, InvocationKind.EXACTLY_ONCE) }
    val task = RunnableTask(runnable)
    return addChild(task)
}

@OptIn(ExperimentalContracts::class)
@GdxAiDsl
fun <E> Task<E>.waitUntil(
    condition: (E) -> Boolean
): Int {
    contract { callsInPlace(condition, InvocationKind.AT_LEAST_ONCE) }
    val task = CustomTask<E>(
        onExecute = {
            if (condition.invoke(`object`)) Task.Status.SUCCEEDED else Task.Status.RUNNING
        }
    )
    return addChild(task)
}

@OptIn(ExperimentalContracts::class)
@GdxAiDsl
fun <E> Task<E>.awaitCompletion(
    onStart: (E, onCompleteCallback: () -> Unit) -> Unit
): Int {
    contract { callsInPlace(onStart, InvocationKind.EXACTLY_ONCE) }
    val task = AwaitCompletionTask(onStart)
    return addChild(task)
}

@OptIn(ExperimentalContracts::class)
@GdxAiDsl
inline fun <E> Task<E>.timeLimit(
    timeLimit: Float,
    task: Task<E>? = null,
    succeedOnTimeOut: Boolean = false,
    init: (@GdxAiDsl TimeLimitDecorator<E>).() -> Unit = {}
): Int {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    val decorator = TimeLimitDecorator(timeLimit, task)
    decorator.succeedOnTimeOut = succeedOnTimeOut
    decorator.init()
    return addChild(decorator)
}

@OptIn(ExperimentalContracts::class)
@GdxAiDsl
inline fun <E> Task<E>.resetOnCompletion(
    task: Task<E>? = null,
    init: (@GdxAiDsl ResetOnCompletionDecorator<E>).() -> Unit = {}
): Int {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    val decorator = ResetOnCompletionDecorator(task)
    decorator.init()
    return addChild(decorator)
}

/**
 * Patched parallel task that properly reset its child tasks.
 * @see ktx.ai.parallel
 */
@OptIn(ExperimentalContracts::class)
@GdxAiDsl
inline fun <E> Task<E>.parallelPatched(
    policy: Parallel.Policy = Parallel.Policy.Sequence,
    orchestrator: Parallel.Orchestrator = Parallel.Orchestrator.Resume,
    tasks: Array<Task<E>> = Array(),
    init: (@GdxAiDsl Parallel<E>).() -> Unit = {},
): Int {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    val parallel = object : Parallel<E>(policy, orchestrator, tasks) {
        override fun resetAllChildren() {
            var i = 0
            val n = childCount
            while (i < n) {
                val child = getChild(i)
                child.resetTask()
                i++
            }
        }
    }
    parallel.init()
    return addChild(parallel)
}

class RunnableTask<E>
    (private var runnable: (E) -> Unit) : LeafTask<E>() {

    override fun copyTo(task: Task<E>): Task<E> {
        val runnableTask = task as RunnableTask<E>
        runnableTask.runnable = runnable
        return task;
    }

    override fun execute(): Status {
        runnable(`object`)
        return Status.SUCCEEDED
    }
}

class TimeLimitDecorator<E>
    (var timeLimit: Float, task: Task<E>? = null) : Decorator<E>(task) {

    var succeedOnTimeOut = false
    private var timeAccumulator = 0f

    override fun run() {
        timeAccumulator += GdxAI.getTimepiece().deltaTime
        if (timeAccumulator > timeLimit) {
            if (succeedOnTimeOut) {
                child.success()
            } else {
                child.fail()
            }
            return
        }
        super.run()
    }

    override fun resetTask() {
        super.resetTask()

        timeAccumulator = 0f
    }
}

class ResetOnCompletionDecorator<E>
    (child: Task<E>? = null) : Decorator<E>(child) {

    override fun end() {
        super.end()
        child.resetTask()
    }
}

//class ConditionGuard<E>(task: Task<E>? = null, private val condition: () -> Boolean) : Decorator<E>(task) {
class ConditionGuard<E>(private val condition: () -> Boolean) : LeafTask<E>() {

    override fun execute(): Status =
        if (condition()) Status.SUCCEEDED else Status.FAILED

    override fun copyTo(task: Task<E>): Task<E> {
        TODO("Not yet implemented")
    }
}

class CustomTask<E>(
    private val guardTask: Task<E>? = null,
    private val onStart: () -> Unit = {},
    private val onEnd: () -> Unit = {},
    private val onResetTask: () -> Unit = {},
    private val onExecute: () -> Status
) :
    LeafTask<E>() {

    init {
        this.guard = guardTask
    }

    override fun start() = onStart()

    override fun end() = onEnd()

    override fun resetTask() = onResetTask()

    override fun execute() = onExecute()

    override fun copyTo(task: Task<E>?): Task<E> {
        TODO("Not yet implemented")
    }
}

class AwaitCompletionTask<E>(
    private val onStart: (E, onCompleteCallback: () -> Unit) -> Unit,
    private val onEnd: (E) -> Unit = { },
): LeafTask<E>() {

    var isCompleted = false
        private set

    override fun start() {
        super.start()

        onStart(`object`) {
            isCompleted = true
        }
    }

    override fun end() {
        super.end()
        onEnd.invoke(`object`)
    }

    override fun resetTask() {
        super.resetTask()
        isCompleted = false
    }

    override fun execute(): Status {
        return if (isCompleted) Status.SUCCEEDED else Status.RUNNING
    }

    override fun copyTo(task: Task<E>?): Task<E> {
        TODO("Not yet implemented")
    }
}