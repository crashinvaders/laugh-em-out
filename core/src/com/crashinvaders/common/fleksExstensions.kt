package com.crashinvaders.common

import com.github.quillraven.fleks.*

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

abstract class EntityComponent<T> : Component<T> {

    lateinit var entity: Entity private set

    override fun World.onAdd(entity: Entity) {
        this@EntityComponent.entity = entity
    }
}