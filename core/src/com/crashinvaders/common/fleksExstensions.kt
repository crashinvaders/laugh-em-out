package com.crashinvaders.common

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.IntervalSystem

typealias FleksWorld = com.github.quillraven.fleks.World

fun IntervalSystem.toggle() {
    enabled = !enabled
}

inline fun Family.firstOrNull(crossinline condition: Family.(Entity) -> Boolean): Entity? {
    var result: Entity? = null
    this.forEach {
        if (condition(it)) {
            result = it
            return@forEach
        }
    }
    return result
}
