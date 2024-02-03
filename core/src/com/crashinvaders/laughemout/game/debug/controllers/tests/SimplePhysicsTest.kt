package com.crashinvaders.laughemout.game.debug.controllers.tests

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.Box2dWorld
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.OrderedInputMultiplexer
import com.github.quillraven.fleks.Entity
import com.crashinvaders.laughemout.game.GameInputOrder
import com.crashinvaders.laughemout.game.debug.controllers.DebugController
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import ktx.app.KtxInputAdapter
import ktx.box2d.body
import ktx.box2d.circle
import ktx.collections.GdxArray
import ktx.math.component1
import ktx.math.component2

class SimplePhysicsTest(private val fleksWorld: FleksWorld) : DebugController {

    private val box2dWorld: Box2dWorld = fleksWorld.inject()
    private val inputMultiplexer: OrderedInputMultiplexer = fleksWorld.inject()
    private val entities = GdxArray<Entity>()
    private val inputHandler: InputProcessor

    private val textureCircleRed = Texture(Gdx.files.internal("textures/circle100-red.png"))
    private val textureCircleGreen = Texture(Gdx.files.internal("textures/circle100-green.png"))
    private val textureCircleBlue = Texture(Gdx.files.internal("textures/circle100-blue.png"))

    init {
        with(fleksWorld) {
            val groundEntity = entity { entity ->
                entity += Info("Static")
                entity += Transform()
                entity += PhysBody(box2dWorld.body {
                    type = BodyDef.BodyType.StaticBody
                    position.set(entity[Transform].localPosition)
                    fixedRotation = true

                    circle {
                        it.radius = 1f
                        it.position = Vector2.Zero
                    }
                })

                entity += ActorContainer(Image(textureCircleRed))
                entity += DrawableRenderer(ActorEntityRenderer)
                entity += DrawableOrder()
                entity += DrawableTint()
                entity += DrawableVisibility()
                entity += DrawableDimensions(2f, 2f)
                entity += DrawableOrigin(Align.center)

                entity += TransformDebugRenderTag
            }.also { entities.add(it) }
            entities + entity { entity ->
                entity += Info("Dynamic")
//                entity += SodInterpolation(4f, 0.2f, 2f)
                entity += Transform().apply {
                    parent = groundEntity[Transform]
                    ignoreParent = true
                    localPositionX = 0.1f
                    localPositionY = 3.0f
                }
                entity += PhysBody(box2dWorld.body {
                    type = BodyDef.BodyType.DynamicBody
                    position.set(entity[Transform].localPosition)
                    fixedRotation = false

                    circle {
                        it.radius = 1f
                        it.position = Vector2.Zero
                        val weight = 1f
                        density = weight / (it.radius * it.radius * MathUtils.PI)
                    }
                })
                entity += PhysTransformMapperTag.PHYS_TO_TRANSFORM

                entity += ActorContainer(Image(textureCircleGreen))
                entity += DrawableRenderer(ActorEntityRenderer)
                entity += DrawableOrder()
                entity += DrawableTint()
                entity += DrawableVisibility()
                entity += DrawableDimensions(2f, 2f)
                entity += DrawableOrigin(Align.center)

                entity += TransformDebugRenderTag
            }.also { entities.add(it) }
            val kinematicEntity = entity { entity ->
                entity += Info("Kinematic")
//                entity += SodInterpolation(4f, 0.2f, 2f)
                entity += Transform().apply {
                    localPositionX = 2.0f
                    localPositionY = -1.0f
                }
                entity += PhysBody(box2dWorld.body {
                    type = BodyDef.BodyType.KinematicBody
                    position.set(entity[Transform].localPosition)
                    fixedRotation = false

                    circle {
                        it.radius = 1f
                        it.position = Vector2.Zero
                        val weight = 1f
                        density = weight / (it.radius * it.radius * MathUtils.PI)
                    }
                })
                entity += PhysTransformMapperTag.TRANSFORM_TO_PHYS

                entity += ActorContainer(Image(textureCircleBlue))
                entity += DrawableRenderer(ActorEntityRenderer)
                entity += DrawableOrder()
                entity += DrawableTint()
                entity += DrawableVisibility()
                entity += DrawableDimensions(2f, 2f)
                entity += DrawableOrigin(Align.center)

                entity += TransformDebugRenderTag
            }.also { entities.add(it) }

            inputHandler = object : KtxInputAdapter {
                val camera = fleksWorld.system<MainCameraStateSystem>().camera
                val tmpVec3 = Vector3()

                override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                    val (worldX, worldY) = camera.unproject(tmpVec3.set(screenX.toFloat(), screenY.toFloat(), 0f))
                    kinematicEntity[Transform].setWorldPosition(worldX, worldY)
                    return true;
                }
            }
            inputMultiplexer.addProcessor(inputHandler, GameInputOrder.DEBUG_CONTROLLERS)
        }
    }

    override fun dispose() {
        inputMultiplexer.removeProcessor(inputHandler)

        textureCircleRed.dispose()
        textureCircleGreen.dispose()
        textureCircleBlue.dispose()

        for (entity in entities) {
            fleksWorld -= entity
        }
    }
}
