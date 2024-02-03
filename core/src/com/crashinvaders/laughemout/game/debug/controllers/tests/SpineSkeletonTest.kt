package com.crashinvaders.laughemout.game.debug.controllers.tests

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.OrderedInputMultiplexer
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.utils.SkeletonActor
import com.github.quillraven.fleks.Entity
import com.crashinvaders.laughemout.game.GameInputOrder
import com.crashinvaders.laughemout.game.debug.controllers.DebugController
import com.crashinvaders.laughemout.game.engine.components.ActorContainer
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import ktx.app.KtxInputAdapter
import ktx.math.component1
import ktx.math.component2

class SpineSkeletonTest(private val fleksWorld: FleksWorld) : KtxInputAdapter, DebugController {

    private val entity: Entity

    private val atlas = TextureAtlas(Gdx.files.internal("skeletons/spineboy-pro.atlas"))

    private val inputMultiplexer: OrderedInputMultiplexer = fleksWorld.inject()

    private var transform: Transform

    private val camSystem: MainCameraStateSystem

    init {
        camSystem = fleksWorld.system<MainCameraStateSystem>()

        val skelData = SkeletonBinary(atlas)
            .apply { scale = 0.005f }
            .readSkeletonData(Gdx.files.internal("skeletons/spineboy-pro.skel"))
        skelData.defaultSkin = skelData.skins[0]
        val skelActor = SkeletonActor(
            fleksWorld.inject<SkeletonRenderer>(),
            Skeleton(skelData),
            AnimationState(AnimationStateData(skelData)).apply {
                setAnimation(0, "run", true)
            })

        with(fleksWorld) {
            entity = entity {entity ->
                entity += Info("SkeletonTest")
                entity += Transform().apply {
                    localPositionX = 0f
                    localPositionY = 0f
                }

                entity += ActorContainer(skelActor)
                entity += DrawableRenderer(ActorEntityRenderer)
                entity += DrawableOrder()
                entity += DrawableTint()
                entity += DrawableVisibility()
                entity += DrawableDimensions(0f)
                entity += DrawableOrigin(Align.bottom)

//                entity += SodInterpolation(4f, 0.4f, -0.5f)
//                entity += TransformDebugRenderTag
            }

            transform = entity[Transform]

            inputMultiplexer.addProcessor(this@SpineSkeletonTest, GameInputOrder.DEBUG_CONTROLLERS)
        }
    }

    override fun dispose() {
        fleksWorld -= entity

        atlas.dispose()

        inputMultiplexer.removeProcessor(this@SpineSkeletonTest)
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val (x, y) = screenToWorld(screenX, screenY)
        transform.setWorldPosition(x, y)
        return true
    }

    private fun screenToWorld(screenX: Int, screenY: Int): Vector3 =
        camSystem.camera.unproject(tmpVec.set(screenX.toFloat(), screenY.toFloat(), 0f))

    companion object {
        private val tmpVec = Vector3()
    }
}
