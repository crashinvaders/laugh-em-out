package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.crashinvaders.common.Box2dWorld
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject

class PhysDebugRenderSystem : IntervalSystem(), OnWorldInitializedHandler {

    private val debugRenderer = Box2DDebugRenderer()

    private val box2dWorld: Box2dWorld = inject()

    private lateinit var mainCam: Camera

    override fun onWorldInitialized() {
        mainCam = world.system<MainCameraStateSystem>().camera
    }

    override fun onDispose() {
        super.onDispose()
        debugRenderer.dispose()
    }

    override fun onTick() {
        val projMat = mainCam.combined
        debugRenderer.render(box2dWorld, projMat)
    }
}
