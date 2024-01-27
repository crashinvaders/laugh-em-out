package com.crashinvaders.laughemout.game.engine.components;

import com.badlogic.gdx.graphics.OrthographicCamera
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

class WorldCamera(
    var ppu: Float,
    val camera: OrthographicCamera = OrthographicCamera()
) : Component<WorldCamera> {
    companion object : ComponentType<WorldCamera>()
    override fun type() = WorldCamera
}

enum class WorldCameraTag : EntityTags by entityTagOf() {
    MAIN, DEBUG_OVERLAY
}
