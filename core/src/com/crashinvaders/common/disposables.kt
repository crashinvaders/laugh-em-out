package com.crashinvaders.common

import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ObjectIntMap
import ktx.assets.DisposableRegistry
import ktx.assets.dispose
import ktx.collections.GdxArray
import java.util.*

interface OrderedDisposableRegistry : Disposable {
    /**
     * A copy of the registered Disposables. The order does not necessarily represent the registration order.
     */
    val allDisposables: List<Disposable>

    /**
     * Registers [disposable] with this registry.
     * @return true if the item was successfully registered or false if it was already registered.
     */
    fun registerDisposable(disposable: Disposable, order: Int = 0): Boolean

    /**
     * Removes [disposable] from this registry.
     * @return true if the item was successfully removed or false if it was not in the registry.
     */
    fun deregisterDisposable(disposable: Disposable): Boolean

    /**
     * Removes all disposables from the registry without disposing them.
     * @return true if any items were in the registry.
     */
    fun deregisterAllDisposables(): Boolean

    /**
     * Calls [dispose][Disposable.dispose] on each registered [Disposable].
     * Might throw an exception if the assets were already disposed. To prevent that and clear the registry,
     * use [deregisterAll].
     *
     * @see Disposable.dispose
     */
    override fun dispose()

    /**
     * Register this [Disposable] with the [DisposableRegistry].
     * @return this object.
     */
    fun <T : Disposable> T.alsoRegisterDisposable(order: Int = 0): T {
        registerDisposable(this, order)
        return this
    }

    /**
     * Remove this [Disposable] from the [DisposableRegistry] if it is already registered.
     * @return this object.
     */
    fun <T : Disposable> T.alsoDeregisterDisposable(): T {
        deregisterDisposable(this)
        return this
    }
}

/**
 * An implementation of [DisposableRegistry] that can be subclassed or used as a delegate.
 * Allows to store and dispose of multiple [Disposable] instances.
 *
 * @see DisposableRegistry
 */
open class OrderedDisposableContainer : OrderedDisposableRegistry {
    private val registry = ObjectIntMap<Disposable>()
    override val allDisposables: List<Disposable> get() = registry.keys().toList()

    override fun registerDisposable(disposable: Disposable, order: Int): Boolean {
        if (registry.containsKey(disposable)) {
            return false
        }
        registry.put(disposable, order)
        return true
    }
    override fun deregisterDisposable(disposable: Disposable): Boolean {
        if (registry.containsKey(disposable)) {
            return false
        }
        registry.remove(disposable, 0)
        return true
    }

    override fun deregisterAllDisposables(): Boolean {
        val hadItems = registry.size > 0
        registry.clear()
        return hadItems
    }

    override fun dispose() {
        //AC: This is messy, but this gets called rarely so lets keep it like this.
        val entires = GdxArray<Pair<Disposable, Int>>()
        for (entry in registry.entries()) {
            entires.add(Pair(entry.key, entry.value))
        }
        entires.sort { entry0, entry1 -> entry0.second.compareTo(entry1.second) }
        entires.forEach { entry -> entry.first.dispose()}
    }
}
