package com.crashinvaders.common.events

import com.badlogic.gdx.utils.SnapshotArray
import com.crashinvaders.common.use
import ktx.app.gdxError
import ktx.assets.pool
import ktx.collections.component1
import ktx.collections.component2
import ktx.collections.gdxMapOf
import ktx.collections.getOrPut
import kotlin.reflect.KClass

class EventBus {

    private val eventHandlerArrayPool = pool { SnapshotArray<EventHandler>() }

    private val handlerMap = gdxMapOf<KClass<out Event>, SnapshotArray<EventHandler>>()

    fun <TEvent : Event> dispatch(event: TEvent) {
        val eventClass = event::class
        handlerMap.get(eventClass)?.use { it.onEvent(event) }
    }

    inline fun <reified TEvent : Event> addHandler(handler: EventHandler) =
        addHandler(handler, TEvent::class)

    fun <TEvent : Event> addHandler(handler: EventHandler, eventType: KClass<TEvent>) {
        val handlers = handlerMap.getOrPut(eventType) { eventHandlerArrayPool.obtain() }
        if (handlers.contains(handler, true)) {
            gdxError("Duplicated handler for event: " + eventType.simpleName + ", handler: " + handler)
        }
        handlers.add(handler)
    }

    fun <TEvent : Event> addHandlers(handler: EventHandler, vararg eventTypes: KClass<out TEvent>) {
        eventTypes.forEach { addHandler(handler, it) }
    }

    inline fun <reified TEvent : Event> removeHandler(handler: EventHandler) =
        removeHandler(handler, TEvent::class)

    fun <TEvent : Event> removeHandler(handler: EventHandler, eventType: KClass<TEvent>) {
        val handlers = handlerMap.get(eventType)
        if (handlers != null) {
            handlers.removeValue(handler, true)
            if (handlers.isEmpty) {
                handlerMap.remove(eventType)
            }
        }
    }

    fun removeHandlers(handler: EventHandler) {
        handlerMap.removeAll {
            val (_, handlers) = it
            handlers!!.removeValue(handler, true)
            return@removeAll handlers.isEmpty
        }
    }
}

interface Event {

}

fun interface EventHandler {
    fun onEvent(event: Event)
}
