package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.Gdx
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.crashinvaders.laughemout.game.engine.components.WorldCamera
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.Transform
import ktx.math.component1
import ktx.math.component2

class WorldCameraSystem : IteratingSystem(
    family { all(WorldCamera, Transform, Info) }
), com.crashinvaders.laughemout.game.engine.systems.OnWorldResizeHandler {

    private var screenWidth = Gdx.graphics.width
    private var screenHeight = Gdx.graphics.height

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onResize(screenWidth: Int, screenHeight: Int) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight

        family.entities.forEach {
            val worldCamera = it[WorldCamera]

            val ppu = worldCamera.ppu
            val worldWidth = screenWidth / ppu
            val worldHeight = screenHeight / ppu

            val camera = worldCamera.camera
            camera.viewportWidth = worldWidth
            camera.viewportHeight = worldHeight
        }
    }

    override fun onTickEntity(entity: Entity) {
        val worldCamera = entity[WorldCamera]
        val transform = entity[Transform]

        val (scaleX, scaleY) = transform.worldScale

        val ppu = worldCamera.ppu
        val worldWidth = (screenWidth * scaleX) / ppu
        val worldHeight = (screenHeight * scaleY) / ppu

        val camera = worldCamera.camera
        camera.viewportWidth = worldWidth
        camera.viewportHeight = worldHeight
        val (x, y) = transform.worldPosition
        camera.position.set(x, y, 0f)
        camera.update()
    }
}
