package com.crashinvaders.laughemout.game.engine.components;

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class ActionOwner(
    private val onRemoveCallback: (entity: Entity) -> Unit
) : Component<ActionOwner> {

    override fun World.onRemove(entity: Entity) {
        onRemoveCallback(entity)
    }

    override fun type() = ActionOwner

    companion object : ComponentType<ActionOwner>()
}
