package com.crashinvaders.laughemout.game

import com.crashinvaders.common.FleksWorld
import com.github.quillraven.fleks.*
import com.crashinvaders.laughemout.game.engine.components.Info
import ktx.app.gdxError

object WorldHelper {
    //TODO AC: This might be slow and ugly. Find another way to find the entity of the component.
    inline fun <reified T : Component<*>> FleksWorld.findEntity(type: ComponentType<T>, component: T): Entity =
        family { all(type) }.entities
            .find { entity -> entity.getOrNull(type) == component }
            ?: gdxError("Failed to find entity for the provided transform.")

    fun Entity.getPrintName(world: FleksWorld): String {
        val entity = this
        with(world) {
            return entity.getOrNull(Info)?.name ?: "[${entity.id}:${entity.version}]"
        }
    }
}
