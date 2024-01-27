package com.crashinvaders.laughemout.game.debug.controllers.tests

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.OrderedInputMultiplexer
import com.github.quillraven.fleks.Entity
import com.crashinvaders.laughemout.game.GameInputOrder
import com.crashinvaders.laughemout.game.common.camera.Sod2CameraProcessor
import com.crashinvaders.laughemout.game.debug.controllers.DebugController
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.SodInterpolation
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.TransformDebugRenderTag
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.CustomAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.RepeatAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.SequenceAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.transform.MoveToWorldAction
import ktx.app.KtxInputAdapter

class EntityActionSystemTest(private val fleksWorld: FleksWorld) : KtxInputAdapter, DebugController {

    private val camera: Camera
    private val entity: Entity

//    private val textureDosTest0 = Texture(Gdx.files.internal("textures/dos-power-stance0.png"))
    private val textureDosTest0 = Texture(Gdx.files.internal("textures/dos-bald0.png"))

    private val inputMultiplexer: OrderedInputMultiplexer = fleksWorld.inject()

    private val camSystem: MainCameraStateSystem
    private val camProcessor: Sod2CameraProcessor

    init {
        camSystem = fleksWorld.system<MainCameraStateSystem>()

        var transform: Transform

        with(fleksWorld) {
            entity = entity {entity ->
                entity += Info("Root0")
                entity += Transform().apply {
                    localPositionX = 0f
                    localPositionY = 0f
                }

                entity += ActorContainer(Image(textureDosTest0))
                entity += DrawableOrder()
                entity += DrawableTint()
                entity += DrawableVisibility()
                entity += DrawableDimensions(2f, 2f)
                entity += DrawableOrigin(Align.bottom)

                entity += SodInterpolation(4f, 0.4f, -0.5f)
                entity += TransformDebugRenderTag
            }

            transform = entity[Transform]

            camera = fleksWorld.system<MainCameraStateSystem>().camera

            inputMultiplexer.addProcessor(this@EntityActionSystemTest, GameInputOrder.DEBUG_CONTROLLERS)
        }

        val actionSystem = fleksWorld.system<EntityActionSystem>()
        actionSystem.addAction(entity, RepeatAction(
            com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.SequenceAction(
                MoveToWorldAction(2f, 1f, duration = 1f, interpolation = Interpolation.exp5),
                CustomAction(
                    { t ->
                        transform.setWorldPosition(
                            MathUtils.lerp(2f, 0f, t),
                            MathUtils.lerp(1f, 0f, t)
                        )
                    },
                    1f, Interpolation.exp5
                ),
//                MoveToWorldAction(0f, 0f, duration = 1f, interpolation = Interpolation.exp5)
            )
        ))

        camProcessor = object : Sod2CameraProcessor(2f, 1f, 0f) {
            override fun process(camState: MainCameraStateSystem.CamState, deltaTime: Float) {
                this.x = transform.worldPositionX
                this.y = transform.worldPositionY
                super.process(camState, deltaTime)
            }
        }
//        camProcessor = object : MainCameraStateSystem.CamProcessor {
//            private val sod = SecondOrderDynamics2D()
//
//            override fun getOrder(): Int = 0
//            override fun onAdded(camState: MainCameraStateSystem.CamState) {
//                sod.reset(2f, 1f, 0f, camState.x, camState.y)
//            }
//            override fun onRemoved(camState: MainCameraStateSystem.CamState) = Unit
//            override fun process(camState: MainCameraStateSystem.CamState, deltaTime: Float) {
//                sod.posX = camState.x
//                sod.posY = camState.y
//                sod.update(deltaTime, transform.worldPositionX, transform.worldPositionY)
//                camState.x = sod.posX
//                camState.y = sod.posY
//            }
//        }
        camSystem.addProcessor(camProcessor)
    }

    override fun dispose() {
        fleksWorld -= entity

        textureDosTest0.dispose()

        inputMultiplexer.removeProcessor(this@EntityActionSystemTest)

        camSystem.removeProcessor(camProcessor)
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.G -> {
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    fleksWorld.system<EntityActionSystem>().removeActions(entity)
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private val tmpVec = Vector3()
    }
}
