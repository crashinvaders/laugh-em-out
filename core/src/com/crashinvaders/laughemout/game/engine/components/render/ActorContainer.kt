package com.crashinvaders.laughemout.game.engine.components.render;

import com.badlogic.gdx.scenes.scene2d.Actor
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class ActorContainer(val actor: Actor) : Component<ActorContainer> {
    override fun type() = ActorContainer
    companion object : ComponentType<ActorContainer>()
}
