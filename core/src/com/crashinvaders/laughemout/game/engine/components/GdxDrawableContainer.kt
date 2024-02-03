package com.crashinvaders.laughemout.game.engine.components

import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class GdxDrawableContainer(
    val drawable: TransformDrawable
) : Component<GdxDrawableContainer> {

    override fun type() = GdxDrawableContainer
    companion object : ComponentType<GdxDrawableContainer>()
}