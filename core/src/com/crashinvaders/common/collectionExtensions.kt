@file:JvmName("CollectionExtensions")

package com.crashinvaders.common

import com.badlogic.gdx.utils.SnapshotArray
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T> SnapshotArray<T>.use(action: (T) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val size = this.size
    val items = begin()
    for (i in 0 until size) {
        action(items[i])
    }
    end()
}

@OptIn(ExperimentalContracts::class)
inline fun <T> SnapshotArray<T>.useIndexed(action: (T, Int) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val size = this.size
    val items = begin()
    for (i in 0 until size) {
        action(items[i], i)
    }
    end()
}
