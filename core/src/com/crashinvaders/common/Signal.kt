package com.crashinvaders.common

import com.badlogic.gdx.utils.SnapshotArray
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class BlankSignal: Signal<Unit>() {
    operator fun invoke() {
        invoke(invoke(Unit))
    }
}

open class Signal<T> {
    private val observers = SnapshotArray<(T) -> Unit>(4)

    fun hasObservers() = observers.size != 0

    operator fun plusAssign(observer: (T) -> Unit) {
        observers.add(observer)
    }

    operator fun minusAssign(observer: (T) -> Unit) {
        observers.removeValue(observer, true)
    }

    operator fun invoke(value: T) {
        observers.use {
            it.invoke(value)
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun invoke(crossinline payload: () -> T) {
        contract { callsInPlace(payload, InvocationKind.AT_MOST_ONCE) }
        if (hasObservers()) {
            this.invoke(payload())
        }
    }
}
