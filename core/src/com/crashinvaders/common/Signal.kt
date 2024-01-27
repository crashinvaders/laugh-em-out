package com.crashinvaders.common

import com.badlogic.gdx.utils.SnapshotArray

class BlankSignal: Signal<Unit>() {
    operator fun invoke() {
        invoke(invoke(Unit))
    }
}

open class Signal<T> {
    private val observers = SnapshotArray<(T) -> Unit>(4)

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
}
