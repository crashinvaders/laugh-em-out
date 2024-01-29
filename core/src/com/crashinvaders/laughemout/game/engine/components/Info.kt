package com.crashinvaders.laughemout.game.engine.components;

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class Info(var name: String) : Component<Info> {
    override fun type() = Info
    companion object : ComponentType<Info>()
}
