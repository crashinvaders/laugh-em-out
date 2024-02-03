package com.crashinvaders.laughemout.game.engine.components

import com.badlogic.gdx.scenes.scene2d.Actor
import com.crashinvaders.laughemout.game.engine.TimeMode
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class ActorContainer(
    val actor: Actor,
    val timeMode: TimeMode = TimeMode.GameTime,
) : Component<ActorContainer> {
    override fun type() = ActorContainer
    companion object : ComponentType<ActorContainer>()
}