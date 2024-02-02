package com.crashinvaders.laughemout.game.debug.controllers.tests

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
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
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.*
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.transform.TransformSpace
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
                entity += DrawableOrigin(Align.center)

                entity += SodInterpolation(4f, 0.4f, -0.5f).apply {
                    timeMode = SodInterpolation.TimeMode.UnscaledTime
                }
                entity += TransformDebugRenderTag
            }

            transform = entity[Transform]

            camera = fleksWorld.system<MainCameraStateSystem>().camera

            inputMultiplexer.addProcessor(this@EntityActionSystemTest, GameInputOrder.DEBUG_CONTROLLERS)
        }

        fleksWorld.system<EntityActionSystem>().actions(entity) {
            repeat {
                timeMode = Action.TimeMode.UnscaledTime
                sequence {
                    parallel {
                        rotateBy(60f)
                        moveTo(2f, 1f, 1f, Interpolation.exp5, TransformSpace.World)
                    }
                    parallel {
                        rotateBy(60f)
                        interpolate(1f, Interpolation.exp5) { _, progress ->
                            transform.setWorldPosition(
                                MathUtils.lerp(2f, 0f, progress),
                                MathUtils.lerp(1f, 0f, progress)
                            )
                        }
                    }
                }
            }
        }

        camProcessor = object : Sod2CameraProcessor(2f, 1f, 0f) {
            override fun process(camTransform: Transform.Snapshot, deltaTime: Float) {
                this.x = transform.worldPositionX
                this.y = transform.worldPositionY
                super.process(camTransform, deltaTime)
            }
        }
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
                    fleksWorld.system<EntityActionSystem>().removeAllActions(entity)
                    return true
                }
            }
        }
        return false
    }
}
