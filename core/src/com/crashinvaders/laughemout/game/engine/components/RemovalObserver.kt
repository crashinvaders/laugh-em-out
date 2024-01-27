package com.crashinvaders.laughemout.game.engine.components

import com.github.quillraven.fleks.*
import ktx.app.gdxError
import ktx.collections.GdxArray

class RemovalObserver : Component<RemovalObserver> {

    constructor()

    constructor(listener: Listener) {
        addListener(listener)
    }

    private val observers = GdxArray<Listener>()

    fun addListener(listener: Listener) {
        observers.add(listener)
    }

    fun removeListener(listener: Listener): Boolean {
        return observers.removeValue(listener, true)
    }

    override fun World.onRemove(entity: Entity) {
        for (i in 0 until observers.size) {
            observers[i].onRemoved(entity)
        }
    }

    override fun type() = RemovalObserver

    companion object : ComponentType<RemovalObserver>() {

        fun EntityComponentContext.addRemovalListener(entity: Entity, listener: Listener): Listener {
            val observer: RemovalObserver = obtainObserverComp(entity)
            observer.addListener(listener)
            return listener
        }

        fun EntityComponentContext.removeRemovalListener(entity: Entity, listener: Listener) {
            val observer: RemovalObserver = obtainObserverComp(entity)
            if (!observer.removeListener(listener))
                gdxError("The entity [${entity.id}] doesn't have the listener registered in the RemovalObserver component.")
        }

        private fun EntityComponentContext.obtainObserverComp(entity: Entity): RemovalObserver {
            val component = entity.getOrNull(RemovalObserver)
            if (component != null)
                return component

            entity.configure {
                it += RemovalObserver()
            }
            return entity[RemovalObserver]
        }
    }

    fun interface Listener {
        fun onRemoved(entity: Entity)
    }
}
